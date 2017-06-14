package oen.bard2.html

import oen.bard2.youtube.SearchResult
import oen.bard2._
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.MouseEvent

import scalatags.JsDom.all._

class HtmlDresser {
  def dressPlaylistPosition(playlistPosition: PlaylistPosition, index: Int, send: Data => Unit): Div = {

    val playButton = a(cls := "btn-floating btn waves-effect waves-light", i(cls := "material-icons", "play_arrow")).render
    playButton.onclick = (_: MouseEvent) => send(Play(index))

    val deleteButton = a(cls := "btn-floating btn waves-effect waves-light", i(cls := "material-icons", "delete_forever")).render
    deleteButton.onclick = (_: MouseEvent) => {
      val delete = DeleteFromPlaylist(playlistPosition.ytHash, index)
      send(delete)
    }

    div(cls := "collection-item valign-wrapper",
        div(cls := "col s3 m3 l3", deleteButton),
        div(cls := "col s3 m3 l3", img(cls := "yt-thumbnail", src := s"https://img.youtube.com/vi/${playlistPosition.ytHash}/hqdefault.jpg")),
        div(cls := "col s3 m3 l3", s"${playlistPosition.title} ${playlistPosition.duration}sec"),
        div(cls := "col s3 m3 l3", playButton)
    ).render
  }

  def dressYtSearchResult(searchResult: SearchResult, send: Data => Unit): Div = {

    val addButton = a(cls := "btn-floating btn waves-effect waves-light", i(cls := "material-icons", "add")).render
    addButton.onclick = (_: MouseEvent) => {
      val atp = AddToPlaylist(searchResult.videoId)
      send(atp)
    }
    div(cls := "collection-item valign-wrapper",
      div(cls := "col s4 m4 l4", img(cls := "yt-thumbnail", src := searchResult.thumbnail)),
      div(cls := "col s4 m4 l4", s"${searchResult.title}"),
      div(cls := "col s4 m4 l4", addButton)
    ).render
  }
}
