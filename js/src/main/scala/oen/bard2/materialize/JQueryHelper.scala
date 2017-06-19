package oen.bard2.materialize

import org.scalajs.jquery

class JQueryHelper {
  def initMaterialize(): Unit = {
    jquery.jQuery(".modal").asInstanceOf[ModalOperations].modal(new ModalOptions { dismissible = false })
    refreshTooltips()
  }

  def openRoomDeletedModal(): Unit = {
    jquery.jQuery("#room-deleted-modal").asInstanceOf[ModalOperations].modal("open")
  }

  def closeRoomDeletedModal(): Unit = {
    jquery.jQuery("#room-deleted-modal").asInstanceOf[ModalOperations].modal("close")
  }

  def refreshTooltips(): Unit = {
    jquery.jQuery(".tooltipped").asInstanceOf[TooltipsOperations].tooltip(new TooltipsOptions)
  }
}
