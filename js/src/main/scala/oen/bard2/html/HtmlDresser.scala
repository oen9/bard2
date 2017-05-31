package oen.bard2.html

import oen.bard2.youtube.PlayerHelper
import oen.bard2.{Data, PlaylistPosition}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.MouseEvent

import scalatags.JsDom.all._

class HtmlDresser(
                 playerHelper: PlayerHelper // TODO only for test
                 ) {
  def dressPlaylistPosition(playlistPosition: PlaylistPosition, send: Data => Unit): Div = {

    val playButton = a(cls := "btn-floating btn waves-effect waves-light", i(cls := "material-icons", "play_arrow")).render
    playButton.onclick = (_: MouseEvent) => playerHelper.player.foreach(p => {
      p.cueVideoById(playlistPosition.ytHash, 0)
      p.playVideo()
    })

    div(cls := "collection-item",
      div(cls := "row",
        div(cls := "col s4 m4 l4", img(cls := "yt-thumbnai", src := s"https://img.youtube.com/vi/${playlistPosition.ytHash}/0.jpg")),
        div(cls := "col s4 m4 l4", s"${playlistPosition.title} - ${playlistPosition.duration}"),
        div(cls := "col s4 m4 l4", playButton)
      )
    ).render
  }
}
