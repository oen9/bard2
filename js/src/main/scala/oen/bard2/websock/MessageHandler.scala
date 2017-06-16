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
        for (p <- cacheData.playing if p.index == index) {
          cacheData.playing = None
          playerHelper.stop()
        }
        cacheData.playlist = cacheData.playlist.take(index) ++ cacheData.playlist.drop(index + 1)
        refreshPlaylist(send)

      case pp: PlaylistPosition =>
        val ppDressed = htmlDresser.dressPlaylistPosition(pp, cacheData.playlist.size, send, cacheData.playing)
        staticComponents.playlist.appendChild(ppDressed)
        cacheData.playlist = cacheData.playlist :+ pp

      case Play(index, startSeconds) =>
        cacheData.playlist.lift(index).foreach(p => {
          playerHelper.play(p.ytHash, startSeconds)
          cacheData.playing = Some(Playing(index))
          refreshPlaylist(send)
        })

      case Pause =>
        playerHelper.pause()

      case unhandled => println(data) // TODO
    }
  }

  protected def refreshPlaylist(send: Data => Unit) = {
    staticComponents.playlist.innerHTML = ""
    val playlistElements = cacheData.playlist
      .zipWithIndex
      .map(pp => htmlDresser.dressPlaylistPosition(pp._1, pp._2, send, cacheData.playing))

    playlistElements.foreach(staticComponents.playlist.appendChild)

    cacheData.playing.foreach(p => {
      val pElem = playlistElements(p.index)
      val container = staticComponents.playlist

      def isAbove = pElem.offsetTop + pElem.offsetHeight < container.scrollTop
      def isBelow = pElem.offsetTop > container.offsetHeight + container.scrollTop

      if (isAbove || isBelow) container.scrollTop = pElem.offsetTop
    })
  }
}
