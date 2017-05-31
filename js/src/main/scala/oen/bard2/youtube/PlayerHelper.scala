package oen.bard2.youtube

import scala.scalajs.js

class PlayerHelper {
  protected var _player: Option[Player] = None

  def player = _player

  protected def player_=(player: Option[Player]) {
    _player = player
  }

  def loadIframe(): Unit = {
    val tag = org.scalajs.dom.document.createElement("script").asInstanceOf[org.scalajs.dom.html.Script]
    tag.src = "https://www.youtube.com/iframe_api"
    val firstScriptTag = org.scalajs.dom.document.getElementsByTagName("script").item(0)
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag)

    org.scalajs.dom.window.asInstanceOf[js.Dynamic].onYouTubeIframeAPIReady = () => {
      refreshPlayer()
    }
  }

  def refreshPlayer(): Player = {
    val newPlayer = new Player("player", PlayerOptions(
      width = "95%",
      height = "95%",
      videoId = "",
      events = PlayerEvents(
        onStateChange = (e: Event) => {}
      )
    ))
    player = Some(newPlayer)

    newPlayer
  }

}
