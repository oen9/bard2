package oen.bard2.components

import oen.bard2.html.HtmlContent
import oen.bard2.websock.WebsockConnector
import oen.bard2.{AddToPlaylist, AjaxHelper, CreateRoom, Room}
import org.scalajs.dom.{Event, KeyboardEvent, MouseEvent}

class ComponentsLogic(staticComponents: StaticComponents,
                      cacheData: CacheData,
                      ajaxHelper: AjaxHelper,
                      htmlContent: HtmlContent,
                      websockConnector: WebsockConnector) {

  def init(): Unit = {
    staticComponents.newRoomInput.onkeydown = (e: KeyboardEvent) => if ("Enter" == e.key) addRoom()
    staticComponents.newRoomButton.onclick = (_: MouseEvent) => addRoom()
    staticComponents.roomSearchInput.onkeyup = (e: Event) => searchRoom()

    staticComponents.addToPlaylistInput.onkeydown = (e: KeyboardEvent) => if ("Enter" == e.key) addToPlaylist()
    staticComponents.addToPlaylistButton.onclick = (_: MouseEvent) => addToPlaylist()
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

  protected def addToPlaylist(): Unit = {
    val newPlaylistPosition = staticComponents.addToPlaylistInput.value
    if (!newPlaylistPosition.isEmpty) {

      val atp = AddToPlaylist(newPlaylistPosition)
      websockConnector.send(atp)
      staticComponents.addToPlaylistInput.value = ""
    }
  }

}
