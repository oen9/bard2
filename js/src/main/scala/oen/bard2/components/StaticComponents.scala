package oen.bard2.components

import org.scalajs.dom.html.{Anchor, Div, Input}

import scalatags.JsDom.all._

case class StaticComponents (
  newRoomInput: Input = input(id := "new_room_name", `type` := "text").render,
  newRoomButton: Anchor = a(cls := "btn-floating btn-large waves-effect waves-light", i(cls := "material-icons", "add_box")).render,
  roomSearchInput: Input = input(id := "search", `type` := "search").render,
  roomDeleteButton: Anchor = a(
    cls := "waves-effect waves-light btn-floating disabled tooltipped",
    attr("data-tooltip") := "delete room",
    i(cls := "material-icons", "delete_forever")
  ).render,

  roomDeletedBackButton: Anchor = a(cls := "btn-large waves-effect waves-light", href := "#", "back to main page").render,

  ytSearchVideoInput: Input = input(id := "search_video", `type` := "text").render,
  ytSearchVideoButton: Anchor = a(cls := "btn-floating btn-large waves-effect waves-light", i(cls := "material-icons", "search")).render,
  ytSearchResult: Div = div(cls := "collection yt-search-result").render,
  ytSearchPrevButton: Anchor = a(cls := "btn-floating btn-large waves-effect waves-light disabled", i(cls := "material-icons", "navigate_before")).render,
  ytSearchNextButton: Anchor = a(cls := "btn-floating btn-large waves-effect waves-light disabled", i(cls := "material-icons", "navigate_next")).render,

  playlist: Div = div(cls := "collection playlist").render,

  progressbar: Div = div(cls := "progress", div(cls := "indeterminate")).render,
  roomList: Div = div(cls := "collection rooms").render
)
