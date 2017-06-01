package oen.bard2

import oen.bard2.components.{CacheData, ComponentsLogic, StaticComponents}
import oen.bard2.html.{HtmlContent, HtmlDresser}
import oen.bard2.websock.{MessageHandler, WebsockConnector}
import oen.bard2.youtube.PlayerHelper
import org.scalajs.dom.html.Element

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("oen.bard2")
object Main {

  @JSExport
  def main(header: Element, main: Element, footer: Element): Unit = {
    lazy val staticComponents = StaticComponents()
    lazy val cacheData = new CacheData
    lazy val playerHelper = new PlayerHelper(cacheData)

    lazy val htmlDresser = new HtmlDresser
    lazy val messageHandler = new MessageHandler(htmlDresser, staticComponents, playerHelper, cacheData)
    lazy val websockConnector = new WebsockConnector(cacheData, messageHandler)
    playerHelper.send = Some(websockConnector.send)

    lazy val ajaxHelper = new AjaxHelper(cacheData)
    lazy val htmlContent = new HtmlContent(staticComponents, ajaxHelper, playerHelper, cacheData, websockConnector)

    lazy val componentsLogic = new ComponentsLogic(staticComponents, cacheData, ajaxHelper, htmlContent, websockConnector)

    htmlContent.init(header, main, footer)
    componentsLogic.init()
  }
}
