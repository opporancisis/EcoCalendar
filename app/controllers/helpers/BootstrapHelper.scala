package controllers.helpers

import views.html.helper.FieldConstructor

object BootstrapHelper {
  implicit val myFields = FieldConstructor(views.html.bootstrap.input.f)
}