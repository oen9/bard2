package oen.bard2

import oen.bard2.components.{CacheData, ComponentsLogic, StaticComponents}
import oen.bard2.websock.{MessageHandler, WebsockConnector}
import oen.bard2.youtube.PlayerHelper
import org.scalajs.dom.html

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("oen.bard2")
object Main {

  @JSExport
  def main(header: html.Element, main: html.Element, footer: html.Element): Unit = {

    lazy val cacheData = new CacheData
    lazy val ajaxHelper = new AjaxHelper(cacheData)

    lazy val messageHandler = new MessageHandler
    lazy val websockConnector = new WebsockConnector(cacheData, messageHandler)

    lazy val staticComponents = StaticComponents()
    lazy val playerHelper = new PlayerHelper
    lazy val htmlContent = new HtmlContent(staticComponents, ajaxHelper, playerHelper, cacheData, websockConnector)

    lazy val componentsLogic = new ComponentsLogic(staticComponents, cacheData, ajaxHelper, htmlContent)

    htmlContent.init(header, main, footer)
    componentsLogic.init()
  }
}
