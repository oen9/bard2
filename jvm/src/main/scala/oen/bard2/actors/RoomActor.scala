package oen.bard2.actors

import akka.actor.{ActorLogging, ActorRef, Cancellable, Props, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.persistence.PersistentActor
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import oen.bard2._
import oen.bard2.actors.RoomActor.{AddedToPlaylist, CreateUser, RoomState}
import oen.bard2.youtube.Video

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class RoomActor(roomName: String) extends PersistentActor with ActorLogging {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  var users: Set[ActorRef] = Set()
  var playlist: Vector[PlaylistPosition] = Vector()
  var videoCounter: Option[Cancellable] = None

  val youtubeApiKey: String = context.system.settings.config.getString("youtube.api.key")
  val videoUrl = s"""https://www.googleapis.com/youtube/v3/videos?id=%s&key=$youtubeApiKey&part=contentDetails,snippet"""

  val snapshotInterval = 1000
  override def persistenceId: String = roomName

  override def receiveRecover: Receive = {
    case atp: AddedToPlaylist =>
      handleNewPlaylistPosition(atp.playlistPosition)

    case RoomState(pl) =>
      pl.foreach(handleNewPlaylistPosition)

    case e => println(e)
  }

  override def receiveCommand: Receive = {
    case CreateUser(receiver) =>
      val user = context.actorOf(UserActor.props(self))
      context.watch(user)
      users = users + user
      receiver ! user

    case GetPlaylist =>
      sender() ! UserActor.ToOut(Playlist(playlist))

    case AddToPlaylist(ytHash) =>
      fetchVideoInfo(ytHash)

    case pp: PlaylistPosition =>
      persistAsync(AddedToPlaylist(pp))(atp => handleNewPlaylistPosition(atp.playlistPosition))

    case Terminated(terminated) =>
      users = users - terminated

    case e =>
      log.info(e.toString)  // TODO
  }

  def handleNewPlaylistPosition(pp: PlaylistPosition) = {
    playlist = playlist :+  pp
    users.foreach(_ ! UserActor.ToOut(pp))

    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0)
      saveSnapshot(RoomState(playlist))
  }

  def fetchVideoInfo(ytHash: String): Unit = {
    val url = videoUrl.format(ytHash)

    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import akka.pattern._
    import context.dispatcher

    Http(context.system)
      .singleRequest(HttpRequest(uri = url))
      .flatMap(httpR => Unmarshal(httpR.entity).to[Video])
      .map(v => v.items.headOption)
      .map(oi => oi.map(i => PlaylistPosition(ytHash, i.snippet.title, i.contentDetails.duration)))
      .filter(_.isDefined)
      .map(o => o.get)
      .pipeTo(self)
  }
}

object RoomActor {
  def props(roomName: String) = Props(new RoomActor(roomName))

  case class CreateUser(receiver: ActorRef)

  sealed trait Evt
  case class AddedToPlaylist(playlistPosition: PlaylistPosition) extends Evt
  case class RoomState(playlist: Vector[PlaylistPosition]) extends Evt

  val SYNCHRO_MARGIN: FiniteDuration = 2 seconds
}
