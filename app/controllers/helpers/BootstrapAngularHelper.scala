package controllers.helpers

import views.html.helper.FieldConstructor

object BootstrapAngularHelper {
  implicit val myFields = FieldConstructor(views.html.bootstrap.angular.ngField.f)
}