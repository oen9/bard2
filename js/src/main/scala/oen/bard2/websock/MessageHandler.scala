package oen.bard2.websock

import oen.bard2.components.{CacheData, Playing, StaticComponents}
import oen.bard2.html.HtmlDresser
import oen.bard2.youtube.PlayerHelper
import oen.bard2._

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
        for { p <- cacheData.playing if p.index == index } {
          cacheData.playing = None
          playerHelper.stop()
        }
        cacheData.playlist = cacheData.playlist.take(index) ++ cacheData.playlist.drop(index + 1)
        refreshPlaylist(send)

      case pp: PlaylistPosition =>
        val ppDressed = htmlDresser.dressPlaylistPosition(pp, cacheData.playlist.size, send)
        staticComponents.playlist.appendChild(ppDressed)
        cacheData.playlist = cacheData.playlist :+ pp

      case Play(index, startSeconds) =>
        cacheData.playlist.lift(index).foreach(p => {
          playerHelper.play(p.ytHash, startSeconds)
          cacheData.playing = Some(Playing(index))
        })

      case Pause =>
        playerHelper.pause()

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
