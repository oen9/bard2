package oen.bard2

import oen.bard2.components.CacheData
import org.scalajs.dom.ext.Ajax

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

  def sendPing(): Unit = {
    Ajax.get("/ping")
  }
}
