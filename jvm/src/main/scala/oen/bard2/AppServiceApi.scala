package oen.bard2

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern._
import akka.util.Timeout
import oen.bard2.actors.RoomsActor

import scala.concurrent.duration.DurationLong
import scala.util.{Failure, Success}

class AppServiceApi(
  val system: ActorSystem,
  val roomsActor: ActorRef
) extends AppService

trait AppService {

  implicit def system: ActorSystem
  def roomsActor: ActorRef

  val routes: Route = getStatic ~
    getStaticDev ~
    roomsApi

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

  implicit val timeout = Timeout(5.seconds)

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

  protected def handleNewRoom(newRoomJson: String): Route = {
    Data.fromJson(newRoomJson) match {
      case cr: CreateRoom => onComplete((roomsActor ? cr).mapTo[Data]) {
        case Success(d) => complete(HttpEntity(ContentTypes.`application/json`, Data.toJson(d)))
        case Failure(f) => failWith(f)
      }
      case _ => reject()
    }
  }

}
