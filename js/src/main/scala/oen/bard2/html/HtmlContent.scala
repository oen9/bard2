package oen.bard2.html

import oen.bard2.components.{CacheData, StaticComponents}
import oen.bard2.websock.WebsockConnector
import oen.bard2.youtube._
import oen.bard2.{AjaxHelper, JsUtils, Room}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.HashChangeEvent
import scalatags.JsDom.all._
import scalatags.JsDom.tags2

import scala.scalajs.js.URIUtils

class HtmlContent(staticComponents: StaticComponents,
                  ajaxHelper: AjaxHelper,
                  playerHelper: PlayerHelper,
                  cacheData: CacheData,
                  websockConnector: WebsockConnector) {

  def init(header: html.Element, main: html.Element, footer: html.Element): Unit = {
    readHash()

    val headerContent = initHeader()
    header.appendChild(headerContent)

    val mainContent = initMain()
    main.appendChild(mainContent)

    val footerContent = initFooter()
    footer.appendChild(footerContent)

    playerHelper.loadIframe()
    websockConnector.reConnect()

    dom.window.onhashchange = (e: HashChangeEvent) => {
      onHashChange(main)
    }
  }

  def refreshRooms(rooms: Set[Room]): Unit = {
    staticComponents.roomList.innerHTML = ""

    val sortedRooms = rooms.toSeq.sorted((r1: Room, r2: Room) => JsUtils.localeCompare(r1.name, r2.name))

    sortedRooms.foreach(room => {
      val roomA = a(cls := "collection-item", room.name, href := s"#${room.name}").render
      staticComponents.roomList.appendChild(roomA)
    })
  }

  protected def readHash(): Option[String] = {
    val hash = dom.window.location.hash
    cacheData.roomName = if (hash.isEmpty) None else Some(URIUtils.decodeURI(hash.substring(1)))
    cacheData.roomName
  }

  protected def onHashChange(main: html.Element): Unit = {
    resetState()

    readHash()

    main.innerHTML = ""
    val mainContent = initMain()
    main.appendChild(mainContent)

    playerHelper.refreshPlayer()
    websockConnector.reConnect()
  }

  protected def resetState(): Unit = {
    staticComponents.playlist.innerHTML = ""

    staticComponents.ytSearchVideoInput.value = ""
    staticComponents.newRoomInput.value = ""
    staticComponents.roomSearchInput.value = ""
    staticComponents.roomDeleteButton.classList.add("disabled")

    cacheData.playing = None
    cacheData.playlist = Vector()
    cacheData.roomName = None
  }

  protected def initHeader(): html.Element = {
    tags2.nav(
      div(cls := "nav-wrapper",
        div(cls := "modal", id := "room-deleted-modal",
          div(cls := "modal-content grey-text text-darken-4",
            h4(cls := "", "The room has been closed. Playlist isn't lost. You can re-create this room from main page")
          ),
          div(cls := "modal-footer",
            staticComponents.roomDeletedBackButton
          )
        ),
        ul(cls := "right",
          li(a(cls := "brand-logo right waves-effect waves-light tooltipped", href := "#", attr("data-tooltip") := "home page",
            "bard2",
            a(cls := "material-icons", "list")))
        ),
        ul(cls := "left",
          li(staticComponents.roomDeleteButton)
        )
      )
    ).render
  }

  protected def initMain(): html.Div = {
    cacheData.roomName.fold(enterPoint())(room)
  }

  protected def initFooter(): html.Div = {
    div(cls := "footer-copyright",
      div(cls := "container",
        "© 2017 oen",
        a(cls := "grey-text text-lighten-4 right", target := "_blank", href := "https://github.com/oen9/bard2", "github")
      )
    ).render
  }

  protected def enterPoint(): html.Div = {

    staticComponents.roomList.appendChild(staticComponents.progressbar)
    ajaxHelper.runGetRooms(refreshRooms)

    div(cls := "container center grey lighten-4",
      h4("ROOMS"),

      div(cls := "container",
        div(cls := "row",
          div(cls := "col s11 input-field",
            staticComponents.newRoomInput,
            label(`for` := "new_room_name", "new room name")
          ),
          div(cls := "col s1",
            staticComponents.newRoomButton
          )
        ),

        tags2.nav(
          div(cls := "input-field",
            staticComponents.roomSearchInput,
            label(cls := "label-icon", `for` := "search", i(cls := "material-icons", "search")),
            i(cls := "material-icons", "close")
          )
        ),

        staticComponents.roomList
      )
    ).render
  }

  protected def room(hash: String): html.Div = {
    div(
      div(cls := "row center",
        div(cls := "col s12 m12 l12",
          div(cls := " grey lighten-4 player",
            div(id := "player")
          )
        )
      ),
      div(cls := "row center",
        div(cls := "col s12 m6 l6",
          div(cls := " grey lighten-4",
            h4(s"$hash"),
            staticComponents.playlist
          )
        ),
        div(cls := "col s12 m6 l6",
          div(cls := " grey lighten-4",
            div(cls := "row",
              h4("search"),
              div(cls := "col s12 m11 l11 input-field",
                staticComponents.ytSearchVideoInput,
                label(`for` := "add_to_playlist", "query")
              ),
              div(cls := "col s12 m1 l1", staticComponents.ytSearchVideoButton)
            ),
            div(cls := "grey lighten-4",
              staticComponents.ytSearchPrevButton,
              staticComponents.ytSearchNextButton
            ),
            div(cls := "grey lighten-4",
              staticComponents.ytSearchResult
            )
          )
        )
      )
    ).render
  }
}
