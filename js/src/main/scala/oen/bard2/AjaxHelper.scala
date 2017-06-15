package oen.bard2

import oen.bard2.components.CacheData
import oen.bard2.youtube.{SearchResult, SearchResults}
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

  def runYtSearch(query: String, f: SearchResults => Unit): Unit = {
    runYtSearch(query, None, f)
  }

  def runYtSearch(query: String, token: Option[String], f: SearchResults => Unit): Unit = {
    val encodedQuery = URIUtils.encodeURI(query)
    val baseUri = s"/ytsearch/$encodedQuery"

    token match {
      case Some(t) =>
        val encodedToken = URIUtils.encodeURI(t)
        executeYtSearch(s"$baseUri/$encodedToken", f)

      case None =>
        executeYtSearch(baseUri, f)
    }
  }

  protected def executeYtSearch(query: String, f: SearchResults => Unit): Unit = {
    Ajax.get(query).foreach(response => {
      val results = requestToSearchResult(response)
      f(results)
    })
  }

  def sendPing(): Unit = {
    Ajax.get("/ping")
  }

  protected def requestToSearchResult(req: XMLHttpRequest): SearchResults = {
    val json = js.JSON.parse(req.responseText)

    val prevPageToken = if (js.isUndefined(json.prevPageToken)) None else Some(json.prevPageToken.toString)
    val nextPageToken = if (js.isUndefined(json.nextPageToken)) None else Some(json.nextPageToken.toString)

    val results = json.items.asInstanceOf[js.Array[js.Dynamic]].map(elem => {
      val title = elem.snippet.title.toString
      val thumbnail = elem.snippet.thumbnails.high.url.toString
      val videoId = elem.id.videoId.toString
      SearchResult(title, thumbnail, videoId)
    }).toVector

    SearchResults(prevPageToken, nextPageToken, results)
  }
}
