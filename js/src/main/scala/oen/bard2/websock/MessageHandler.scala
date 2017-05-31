package oen.bard2.websock

import oen.bard2.components.StaticComponents
import oen.bard2.html.HtmlDresser
import oen.bard2.youtube.PlayerHelper
import oen.bard2.{Data, Playlist, PlaylistPosition}

class MessageHandler(htmlDresser: HtmlDresser, staticComponents: StaticComponents, playerHelper: PlayerHelper) {

  def handle(data: Data, send: Data => Unit): Unit = {
    data match {
      case Playlist(playlist) =>
        staticComponents.playlist.innerHTML = ""
        playlist
          .map(pp => htmlDresser.dressPlaylistPosition(pp, send))
          .foreach(staticComponents.playlist.appendChild)

      case pp: PlaylistPosition =>
        val ppDressed = htmlDresser.dressPlaylistPosition(pp, send)
        staticComponents.playlist.appendChild(ppDressed)

      case unhandled => println(data) // TODO
    }
  }
}
