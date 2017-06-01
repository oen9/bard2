package oen.bard2.websock

import oen.bard2.components.{CacheData, StaticComponents}
import oen.bard2.html.HtmlDresser
import oen.bard2.youtube.PlayerHelper
import oen.bard2.{Data, DeleteFromPlaylist, Playlist, PlaylistPosition}

class MessageHandler(htmlDresser: HtmlDresser,
                     staticComponents: StaticComponents,
                     playerHelper: PlayerHelper,
                     cacheData: CacheData) {

  def handle(data: Data, send: Data => Unit): Unit = {
    data match {
      case Playlist(playlist) =>
        cacheData.playlist = playlist
        refreshPlaylist(send)

      case DeleteFromPlaylist(ytHash, index) =>
        cacheData.playlist = cacheData.playlist.take(index) ++ cacheData.playlist.drop(index + 1)
        refreshPlaylist(send)

      case pp: PlaylistPosition =>
        val ppDressed = htmlDresser.dressPlaylistPosition(pp, cacheData.playlist.size, send)
        staticComponents.playlist.appendChild(ppDressed)
        cacheData.playlist = cacheData.playlist :+ pp

      case unhandled => println(data) // TODO
    }
  }

  protected def refreshPlaylist(send: Data => Unit) = {
    staticComponents.playlist.innerHTML = ""
    cacheData.playlist
      .zipWithIndex
      .map(pp => htmlDresser.dressPlaylistPosition(pp._1, pp._2, send))
      .foreach(staticComponents.playlist.appendChild)
  }
}
