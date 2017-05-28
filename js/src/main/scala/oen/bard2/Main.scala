package oen.bard2

import oen.bard2.components.{CacheData, ComponentsLogic, StaticComponents}
import org.scalajs.dom.html

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("oen.bard2")
object Main {

  @JSExport
  def main(header: html.Element, main: html.Element, footer: html.Element): Unit = {

    lazy val cacheData = new CacheData
    lazy val ajaxHelper = new AjaxHelper(cacheData)

    lazy val staticComponents = StaticComponents()
    lazy val htmlContent = new HtmlContent(staticComponents, ajaxHelper)

    lazy val componentsLogic = new ComponentsLogic(staticComponents, cacheData, ajaxHelper, htmlContent)


    htmlContent.init(header, main, footer)
    componentsLogic.init()
  }
}
