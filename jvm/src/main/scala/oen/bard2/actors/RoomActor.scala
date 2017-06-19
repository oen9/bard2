package oen.bard2.actors

import akka.actor.{ActorLogging, ActorRef, Cancellable, Props, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.persistence.PersistentActor
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import oen.bard2._
import oen.bard2.actors.RoomActor._
import oen.bard2.youtube.Video
import org.joda.time.{DateTime, Seconds}

import scala.concurrent.duration.{Duration, DurationDouble, DurationInt, FiniteDuration}

class RoomActor(roomName: String) extends PersistentActor with ActorLogging {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  var users: Set[ActorRef] = Set()
  var playlist: Vector[PlaylistPosition] = Vector()
  var videoCounter: Option[VideoCounter] = None
  var pausedAt: Option[Double] = None

  val youtubeApiKey: String = context.system.settings.config.getString("youtube.api.key")
  val videoUrl = s"""https://www.googleapis.com/youtube/v3/videos?id=%s&key=$youtubeApiKey&part=contentDetails,snippet"""

  val snapshotInterval = 1000
  override def persistenceId: String = roomName

  override def receiveRecover: Receive = {
    case atp: AddedToPlaylist =>
      updatePlaylist(atp.playlistPosition)

    case dfp: DeletedFromPlaylist =>
      updatePlaylist(dfp.deleteFromPlaylist)

    case RoomState(pl) =>
      pl.foreach(updatePlaylist)

    case e => log.info("{}: {}", roomName, e)
  }

  override def receiveCommand: Receive = {
    case CreateUser(receiver) =>
      val user = context.actorOf(UserActor.props(self))
      context.watch(user)
      users = users + user
      receiver ! user

    case GetPlaylist =>
      sender() ! UserActor.ToOut(Playlist(playlist))
      handleInitialState(sender())

    case AddToPlaylist(ytHash) =>
      fetchVideoInfo(ytHash)

    case pp: PlaylistPosition =>
      persistAsync(AddedToPlaylist(pp))(atp => updatePlaylist(atp.playlistPosition))

    case toDelete: DeleteFromPlaylist =>
      persistAsync(DeletedFromPlaylist(toDelete))(dfp => updatePlaylist(dfp.deleteFromPlaylist))

    case p: Play =>
      if (p.index >= 0 && p.index < playlist.size) {
        users.foreach(_ ! UserActor.ToOut(p))
        play(p)
        pausedAt = None
      }

    case Pause =>
      users.foreach(_ ! UserActor.ToOut(Pause))
      videoCounter.foreach(_.cancellable.cancel())
      pausedAt = Some(countCurrentVidTime())

    case ve: VideoEnded =>
      videoCounter.filter(_.videoEnded == ve).foreach(_ => playNext())

    case Terminated(terminated) =>
      users = users - terminated

    case msg: DeleteRoom =>
      users.foreach(_ ! UserActor.ToOut(msg))
      context.stop(self)

    case e =>
      log.info(e.toString)  // TODO
  }

  def updatePlaylist(data: Data) = {
    data match {
      case pp: PlaylistPosition =>
        playlist = playlist :+  pp

      case DeleteFromPlaylist(ytHash, index) =>
        playlist
          .lift(index)
          .filter(_.ytHash == ytHash)
          .foreach(_ =>
            playlist = playlist.take(index) ++ playlist.drop(index + 1)
          )

      case unexpected => log.info("Unexpected message {}", unexpected)
    }

    users.foreach(_ ! UserActor.ToOut(data))
    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0)
      saveSnapshot(RoomState(playlist))
  }

  def fetchVideoInfo(ytHash: String): Unit = {
    val url = videoUrl.format(ytHash)

    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import akka.pattern._
    import context.dispatcher

    (for {
      httpRequest <- Http(context.system).singleRequest(HttpRequest(uri = url))
      video <- Unmarshal(httpRequest.entity).to[Video]
      if video.items.nonEmpty
    } yield {
      val item = video.items.head
      val duration = java.time.Duration.parse(item.contentDetails.duration).getSeconds
      PlaylistPosition(ytHash, item.snippet.title, duration)
    }).pipeTo(self)
  }

  def playNext(): Unit = {
    if (playlist.nonEmpty) {
      val predictedVid = videoCounter.map(_.vid + 1).getOrElse(0)
      val nextVid = playlist.lift(predictedVid).map(_ => predictedVid).getOrElse(0)

      val p = Play(nextVid)
      users.foreach(_ ! UserActor.ToOut(p))

      play(p)
    }
  }

  def play(p: Play): Unit = {
    videoCounter.foreach(_.cancellable.cancel())

    val playlistPosition = playlist(p.index)
    val duration = Duration(playlistPosition.duration, concurrent.duration.SECONDS).plus(SYNCHRO_MARGIN).minus(p.startSeconds seconds)

    val ve = VideoEnded()

    implicit val ex = context.system.dispatcher

    val vc = VideoCounter(
      p.index,
      p.startSeconds,
      DateTime.now(),
      context.system.scheduler.scheduleOnce(duration, self, ve),
      ve
    )

    videoCounter = Some(vc)
  }

  def handleInitialState(sender: ActorRef): Unit = {
    videoCounter
      .filter(!_.cancellable.isCancelled)
      .foreach(vc => {
        val startSeconds = countCurrentVidTime()
        sender ! UserActor.ToOut(Play(vc.vid, startSeconds))
      })

    for (vc <- videoCounter; pauseSeconds <- pausedAt) {
      sender ! UserActor.ToOut(Play(vc.vid, pauseSeconds))
      sender ! UserActor.ToOut(Pause)
    }
  }

  def countCurrentVidTime(): Double = {
    videoCounter.map(vc => {
      val now = DateTime.now()
      val sec = Seconds.secondsBetween(vc.startDateTime, now)
      vc.startSeconds + sec.getSeconds
    }).getOrElse(0)
  }

}

object RoomActor {
  def props(roomName: String) = Props(new RoomActor(roomName))

  case class CreateUser(receiver: ActorRef)

  sealed trait Evt
  case class AddedToPlaylist(playlistPosition: PlaylistPosition) extends Evt
  case class DeletedFromPlaylist(deleteFromPlaylist: DeleteFromPlaylist) extends Evt
  case class RoomState(playlist: Vector[PlaylistPosition]) extends Evt

  val SYNCHRO_MARGIN: FiniteDuration = 2 seconds
  case class VideoCounter(vid: Int,
                          startSeconds: Double,
                          startDateTime: DateTime,
                          cancellable: Cancellable,
                          videoEnded: VideoEnded)
  case class VideoEnded(uuid: String = java.util.UUID.randomUUID().toString)
}
