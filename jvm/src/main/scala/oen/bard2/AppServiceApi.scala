package oen.bard2

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import oen.bard2.actors.{RoomsActor, UserActor}

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong
import scala.util.{Failure, Success}

class AppServiceApi(
  val system: ActorSystem,
  val roomsActor: ActorRef
) extends AppService

trait AppService {

  implicit def system: ActorSystem
  def roomsActor: ActorRef

  implicit val timeout = Timeout(5.seconds)

  val routes: Route = getStatic ~
    getStaticDev ~
    roomsApi ~
    websock ~
    ping

  def getStatic: Route = get {
    pathSingleSlash {
      getFromResource("index.html")
    } ~
    path("bard2-opt.js") {
      getFromResource("bard2-opt.js")
    } ~
    pathPrefix("front-res") {
      getFromResourceDirectory("front-res")
    }
  }

  def getStaticDev: Route = get {
    path("dev") {
      getFromResource("index-dev.html")
    } ~
    path("bard2-fastopt.js") {
      getFromResource("bard2-fastopt.js")
    } ~
    path("bard2-fastopt.js.map") {
      getFromResource("bard2-fastopt.js.map")
    }
  }

  def roomsApi: Route = get {
    path("rooms") {
      onComplete{
        (roomsActor ? RoomsActor.GetRooms).mapTo[Rooms]
      } {
        case Success(r) => complete(HttpEntity(ContentTypes.`application/json`, Data.toJson(r)))
        case Failure(f) => failWith(f)
      }
    }
  } ~
  post {
    path("rooms") {
      entity(as[String])(handleNewRoom)
    }
  }

  def websock: Route = path("websock" / Segment) { roomName =>
    onSuccess(createUser(roomName)) (
      handleWebSocketMessages
    )
  }

  def ping: Route = path("ping") {
    complete("pong")
  }

  protected def handleNewRoom(newRoomJson: String): Route = {
    Data.fromJson(newRoomJson) match {
      case cr: CreateRoom => onComplete((roomsActor ? cr).mapTo[Data]) {
        case Success(d) => complete(HttpEntity(ContentTypes.`application/json`, Data.toJson(d)))
        case Failure(f) => failWith(f)
      }
      case _ => reject()
    }
  }

  protected def createUser(roomName: String): Future[Flow[Message, Message, NotUsed]] = {

    implicit val ex = system.dispatcher

    val createdUser = roomsActor ? RoomsActor.CreateUser(roomName)

    createdUser.map {
      case userActor: ActorRef =>
        createUserFlow(userActor)

      case rnf: RoomNotFound =>
        val data = TextMessage(Data.toJson(rnf))
        Flow.fromSinkAndSource(Sink.ignore, Source.single(data))
    }
  }

  protected def createUserFlow(userActor: ActorRef): Flow[Message, TextMessage.Strict, NotUsed] = {
    val inMsgFlow = Flow[Message]
      .map {
        case TextMessage.Strict(msgText) => Data.fromJson(msgText)
        case _ => NotUsed
      }.to(Sink.actorRef(userActor, PoisonPill))

    val outMsgFlow = Source
      .actorRef[Data](Int.MaxValue, OverflowStrategy.dropTail)
      .mapMaterializedValue(outActor => {
        userActor ! UserActor.Out(outActor)
        NotUsed
      }).map { data: Data => TextMessage(Data.toJson(data)) }

    Flow.fromSinkAndSource(inMsgFlow, outMsgFlow)
  }

}
