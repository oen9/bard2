package oen.bard2

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class AppServiceApi(
  val system: ActorSystem
) extends AppService

trait AppService {

  implicit val system: ActorSystem

  val routes: Route = getStatic ~
    getStaticDev

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
}
