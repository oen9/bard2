package oen.bard2.youtube

import oen.bard2.components.CacheData
import oen.bard2.{Data, Pause, Play}

import scala.scalajs.js

class PlayerHelper(cacheData: CacheData) {

  protected var player: Option[Player] = None
  protected var ignorePlayEvent = false

  var send: Option[Data => Unit] = None

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
        onStateChange = onStateChange(_)
      )
    ))
    player = Some(newPlayer)

    newPlayer
  }

  def play(ytHash: String, startSeconds: Double) = {
    player.foreach(p => {
      p.cueVideoById(ytHash, startSeconds)
      p.playVideo()
      ignorePlayEvent = true
    })
  }

  def stop() = {
    player.foreach(_.stopVideo())
  }

  def pause() = {

    for { p <- player if p.getPlayerState() != Player.State.PAUSED } {
      p.pauseVideo()
      ignorePlayEvent = true
    }
  }

  protected def onStateChange(e: Event) = {

    def onPlaying() = {
      if (ignorePlayEvent) {
        ignorePlayEvent = false
      } else {
        for { playing <- cacheData.playing
              pl <- player
              snd <- send } {
          snd(Play(playing.index, pl.getCurrentTime()))
        }
      }
    }

    def onPaused() = {
      if (ignorePlayEvent) {
        ignorePlayEvent = false
      } else {
        send.foreach(snd => snd(Pause))
      }
    }

    e.data.map(_.toString.toInt).foreach {
      case Player.State.PLAYING => onPlaying()
      case Player.State.PAUSED => onPaused()
      case _ => // ignored
    }
  }

}
