package oen.bard2.html

import oen.bard2.youtube.PlayerHelper
import oen.bard2.{Data, DeleteFromPlaylist, PlaylistPosition}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.MouseEvent

import scalatags.JsDom.all._

class HtmlDresser(
                 playerHelper: PlayerHelper // TODO only for test
                 ) {
  def dressPlaylistPosition(playlistPosition: PlaylistPosition, index: Int, send: Data => Unit): Div = {

    val playButton = a(cls := "btn-floating btn waves-effect waves-light", i(cls := "material-icons", "play_arrow")).render
    playButton.onclick = (_: MouseEvent) => playerHelper.player.foreach(p => {
      p.cueVideoById(playlistPosition.ytHash, 0)
      p.playVideo()
    })

    val deleteButton = a(cls := "btn-floating btn waves-effect waves-light", i(cls := "material-icons", "delete_forever")).render
    deleteButton.onclick = (_: MouseEvent) => {
      val delete = DeleteFromPlaylist(playlistPosition.ytHash, index)
      send(delete)
    }

    div(cls := "collection-item valign-wrapper",
        div(cls := "col s3 m3 l3", deleteButton),
        div(cls := "col s3 m3 l3", img(cls := "yt-thumbnail", src := s"https://img.youtube.com/vi/${playlistPosition.ytHash}/0.jpg")),
        div(cls := "col s3 m3 l3", s"${playlistPosition.title} - ${playlistPosition.duration}"),
        div(cls := "col s3 m3 l3", playButton)
    ).render
  }
}
