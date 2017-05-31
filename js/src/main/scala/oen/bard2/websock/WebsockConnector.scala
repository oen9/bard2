package oen.bard2.websock

import oen.bard2.{Data, GetPlaylist, Ping, RoomNotFound}
import oen.bard2.components.CacheData
import org.scalajs.dom
import org.scalajs.dom.{CloseEvent, Event, WebSocket}

import scala.scalajs.js.URIUtils

class WebsockConnector(cacheData: CacheData, messageHandler: MessageHandler) {

  var websock: Option[WebSocket] = None
  protected var pingIntervalId: Option[Int] = None

  def reConnect(): Unit = {
    close()

    cacheData.roomName.foreach(roomName => {
      val protocol = if ("http:" == dom.window.location.protocol) "ws://" else "wss://"
      val uri = protocol + dom.window.location.host + "/websock/" +  URIUtils.encodeURI(roomName)
      val socket = new dom.WebSocket(uri)
      websock = Some(socket)

      socket.onopen = (_: Event) => {
        pingIntervalId = Some(dom.window.setInterval(() => { socket.send(Data.toJson(Ping)) }, 30000))
        send(GetPlaylist)
      }

      socket.onclose = (_: CloseEvent) => {
        println("Trying to reconnect in 5 seconds")
        dom.window.setTimeout(() => reConnect(), 5000)
      }

      socket.onmessage = (e: dom.MessageEvent) => {
        val data = Data.fromJson(e.data.toString)
        data match {
          case rnf: RoomNotFound =>
            println(s"Room ${rnf.room.name} not found. Closing connection.")
            close()
          case msg =>
            messageHandler.handle(msg, send)
        }
      }

    })
  }

  def send(msg: Data): Unit = {
    websock.foreach(_.send(Data.toJson(msg)))
  }

  def close(): Unit = {
    pingIntervalId.foreach(p => {
      dom.window.clearInterval(p)
      pingIntervalId = None
    })

    websock.foreach(ws => {
      ws.onclose = (_: CloseEvent) => {}
      ws.close()
      websock = None
    })
  }
}
