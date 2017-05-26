package oen.bard2

import org.scalajs.dom.html

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("oen.bard2")
object Main {

  @JSExport
  def main(header: html.Element, main: html.Element, footer: html.Element): Unit = {
    val headerContent = HtmlContent.initHeader()
    header.appendChild(headerContent)

    val mainContent = HtmlContent.initMain()
    main.appendChild(mainContent)

    val footerContent = HtmlContent.initFooter()
    footer.appendChild(footerContent)
  }
}
