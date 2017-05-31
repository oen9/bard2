package oen.bard2.html

import oen.bard2.components.{CacheData, StaticComponents}
import oen.bard2.websock.WebsockConnector
import oen.bard2.youtube._
import oen.bard2.{AjaxHelper, JsUtils, Room}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.{HashChangeEvent, MouseEvent}

import scalatags.JsDom.all._
import scalatags.JsDom.tags2

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
    cacheData.roomName = if (hash.isEmpty) None else Some(hash.substring(1))
    cacheData.roomName
  }

  protected def onHashChange(main: html.Element): Unit = {
    readHash()

    main.innerHTML = ""
    val mainContent = initMain()
    main.appendChild(mainContent)

    playerHelper.refreshPlayer()
    websockConnector.reConnect()
  }

  protected def initHeader(): html.Element = {
    tags2.nav(
      div(cls := "nav-wrapper",
        a(cls := "brand-logo right", href := "#", "bard2"),
        ul(cls := "left",
          li(i(cls := "material-icons right", "not_interested")),
          li(i(cls := "material-icons right", "perm_contact_calendar")),
          li(i(cls := "material-icons right", "power_settings_new"))
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
            a(cls := "btn-floating btn-large waves-effect waves-light", i(cls := "material-icons", "add_box"))
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
    val stopButton = a(cls := "btn-floating btn-large waves-effect waves-light", i(cls := "material-icons", "stop")).render
    stopButton.onclick = (_: MouseEvent) => playerHelper.player.foreach(p => {
      p.stopVideo()
    })

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
            stopButton,
            staticComponents.playlist
          )
        ),
        div(cls := "col s12 m6 l6",
          div(cls := " grey lighten-4",
            h4("add to playlist"),
            div(cls := "row",
              div(cls := "col s12 m11 l11 input-field",
                staticComponents.addToPlaylistInput,
                label(`for` := "add_to_playlist", "yt hash")
              ),
              div(cls := "col s12 m1 l1", staticComponents.addToPlaylistButton)
            )
          )
        )
      )
    ).render
  }
}