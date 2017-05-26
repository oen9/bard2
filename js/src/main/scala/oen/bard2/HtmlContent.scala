package oen.bard2

import org.scalajs.dom.html

import scalatags.JsDom.all._
import scalatags.JsDom.tags2

object HtmlContent {
  def initHeader(): html.Element = {
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

  def initMain(): html.Div = {
    div(cls := "container center grey lighten-4", "hello").render
  }

  def initFooter(): html.Div = {
    div(cls := "footer-copyright",
      div(cls := "container",
        "© 2017 oen",
        a(cls := "grey-text text-lighten-4 right", target := "_blank", href := "https://github.com/oen9/bard2", "github")
      )
    ).render
  }
}
