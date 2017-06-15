package oen.bard2.youtube

import oen.bard2.components.CacheData
import oen.bard2.youtube.PlayerHelper.{Cmd, PauseCmd, PlayCmd, StopCmd}
import oen.bard2.{Data, Pause, Play}

import scala.scalajs.js

class PlayerHelper(cacheData: CacheData) {

  var send: Option[Data => Unit] = None

  protected var player: Option[Player] = None
  protected var ignorePlayEvent = false

  protected var ready = false
  protected var notReadyBuffer: Vector[Cmd] = Vector()

  protected var loadedYtHash: Option[String] = None

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
    ready = false

    val newPlayer = new Player("player", PlayerOptions(
      width = "95%",
      height = "95%",
      videoId = "",
      events = PlayerEvents(
        onReady = onReady(_),
        onStateChange = onStateChange(_)
      )
    ))
    player = Some(newPlayer)
    loadedYtHash = None

    newPlayer
  }

  def play(ytHash: String, startSeconds: Double) = handleReady(PlayCmd(ytHash, startSeconds)) {
    player.foreach(p => {
      if (loadedYtHash.contains(ytHash)) {
        p.seekTo(startSeconds, allowSeekAhead = true)
      } else {
        p.cueVideoById(ytHash, startSeconds)
        loadedYtHash = Some(ytHash)
      }
      p.playVideo()
      ignorePlayEvent = true
    })
  }

  def stop() = handleReady(StopCmd) {
    player.foreach(_.stopVideo())
  }

  def pause() = handleReady(PauseCmd) {
    try {
      for (p <- player if p.getPlayerState() != Player.State.PAUSED) { // fastopt.js cause error when started page with pause
        p.pauseVideo()
        ignorePlayEvent = true
      }
    } catch {
      case _: Throwable =>
        for (p <- player) {
          p.pauseVideo()
          ignorePlayEvent = true
        }
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

  protected def onReady(e: Event) = {
    ready = true

    for (cmd <- notReadyBuffer) {
      cmd match {
        case PlayCmd(ytHash, startSeconds) => play(ytHash, startSeconds)
        case PauseCmd =>
          pause()
          ignorePlayEvent = false // trust me
        case StopCmd => stop()
      }
    }

    notReadyBuffer = Vector()
  }

  protected def handleReady(cmd: Cmd)(f: => Unit) = {
    if (ready) f else notReadyBuffer = notReadyBuffer :+ cmd
  }

}

object PlayerHelper {
  sealed trait Cmd
  case class PlayCmd(ytHash: String, startSeconds: Double) extends Cmd
  case object PauseCmd extends Cmd
  case object StopCmd extends Cmd
}
