package oen.bard2.components

import oen.bard2.html.{HtmlContent, HtmlDresser}
import oen.bard2.websock.WebsockConnector
import oen.bard2.youtube.SearchResults
import oen.bard2.{AjaxHelper, CreateRoom, Room}
import org.scalajs.dom
import org.scalajs.dom.html.{Anchor, Div}
import org.scalajs.dom.{Event, KeyboardEvent, MouseEvent}

class ComponentsLogic(staticComponents: StaticComponents,
                      cacheData: CacheData,
                      ajaxHelper: AjaxHelper,
                      htmlContent: HtmlContent,
                      websockConnector: WebsockConnector,
                      htmlDresser: HtmlDresser) {

  def init(): Unit = {
    staticComponents.newRoomInput.onkeydown = (e: KeyboardEvent) => if ("Enter" == e.key) addRoom()
    staticComponents.newRoomButton.onclick = (_: MouseEvent) => addRoom()
    staticComponents.roomSearchInput.onkeyup = (e: Event) => searchRoom()

    staticComponents.ytSearchVideoInput.onkeydown = (e: KeyboardEvent) => if ("Enter" == e.key) ytSearch()
    staticComponents.ytSearchVideoButton.onclick = (_: MouseEvent) => ytSearch()

    activateHerokuKeepAlive()
  }

  protected def addRoom(): Unit = {
    val newRoomName = staticComponents.newRoomInput.value
    if (!newRoomName .isEmpty) {

      val createRoom = CreateRoom(Room(newRoomName))
      ajaxHelper.runCreateNewRoom(createRoom, {
        staticComponents.newRoomInput.value = ""

        staticComponents.roomList.innerHTML = ""
        staticComponents.roomList.appendChild(staticComponents.progressbar)
        ajaxHelper.runGetRooms(htmlContent.refreshRooms)
      })
    }
  }

  protected def searchRoom(): Unit = {
    val prefix = staticComponents.roomSearchInput.value.toLowerCase
    val filtered = cacheData.rooms.filter(_.name.toLowerCase.startsWith(prefix))
    htmlContent.refreshRooms(filtered)
  }

  protected def ytSearch(): Unit = {
    val query = staticComponents.ytSearchVideoInput.value
    if (!query.isEmpty) {
      ytSearch(query)
    } else {
      staticComponents.ytSearchResult.innerHTML = ""
    }
  }

  protected def ytSearch(query: String): Unit = {
    ytSearch(query, ajaxHelper.runYtSearch(query, _))
  }

  protected def ytSearch(query: String, token: String): Unit = {
    ytSearch(query, ajaxHelper.runYtSearch(query, Some(token), _))
  }

  protected def ytSearch(query: String, f: (SearchResults => Unit) => Unit): Unit = {
    f(searchResults => {
      val dressedResults = searchResults.results.map(r => htmlDresser.dressYtSearchResult(r, websockConnector.send))
      refreshYtSearch(dressedResults)

      confYtNavButton(query, searchResults.prevPageToken, staticComponents.ytSearchPrevButton)
      confYtNavButton(query, searchResults.nextPageToken, staticComponents.ytSearchNextButton)
    })
    staticComponents.ytSearchVideoInput.value = ""
    staticComponents.ytSearchResult.innerHTML = ""
    staticComponents.ytSearchResult.appendChild(staticComponents.progressbar)
  }

  protected def confYtNavButton(query: String, token: Option[String], button: Anchor) = {
    token match {
      case Some(t) =>
        button.onclick = (_: MouseEvent) => ytSearch(query, t)
        button.classList.remove("disabled")
      case None =>
        button.classList.add("disabled")
    }
  }

  protected def activateHerokuKeepAlive(): Unit = {
    val timeout15min = 15 * 60 * 1000
    dom.window.setInterval(() => ajaxHelper.sendPing(), timeout15min)
  }

  protected def refreshYtSearch(elems: Vector[Div] = Vector()) = {
    staticComponents.ytSearchResult.innerHTML = ""
    elems.foreach(staticComponents.ytSearchResult.appendChild)
  }
}
