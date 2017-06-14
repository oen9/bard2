package oen.bard2

import oen.bard2.components.CacheData
import oen.bard2.youtube.SearchResult
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.XMLHttpRequest

import scala.scalajs.js
import scala.scalajs.js.URIUtils

class AjaxHelper(cacheData: CacheData) {
  import scala.concurrent.ExecutionContext.Implicits.global

  def runGetRooms(f: (Set[Room]) => Unit): Unit = {
    Ajax.get("/rooms").foreach(r => {
      Data.fromJson(r.responseText) match {
        case Rooms(rooms)  =>
          cacheData.rooms = rooms
          f(rooms)

        case e => println("unexpected: " + e)
      }
    })
  }

  def runCreateNewRoom(createRoom: CreateRoom, success: => Unit, failed: => Unit = ()): Unit = {
    val json = Data.toJson(createRoom)
    Ajax.post("/rooms", json).foreach(r => {
      Data.fromJson(r.responseText) match {
        case RoomAccepted(room) => success
        case RoomRejected(room) => failed
        case e => println("unexpected: " + e)
      }
    })
  }

  def runYtSearch(query: String, f: Vector[SearchResult] => Unit): Unit = {
    val encodedQuery = URIUtils.encodeURI(query)
    Ajax.get("/ytsearch/" + encodedQuery).foreach(response => {
      val results = requestToSearchResult(response)
      f(results)
    })
  }

  def sendPing(): Unit = {
    Ajax.get("/ping")
  }

  def requestToSearchResult(req: XMLHttpRequest): Vector[SearchResult] = {
    val json = js.JSON.parse(req.responseText)
    println(req.responseText)

    json.items.asInstanceOf[js.Array[js.Dynamic]].map(elem => {
      val title = elem.snippet.title.toString
      val thumbnail = elem.snippet.thumbnails.high.url.toString
      val videoId = elem.id.videoId.toString
      SearchResult(title, thumbnail, videoId)
    }).toVector
  }
}
