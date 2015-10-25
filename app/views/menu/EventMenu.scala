package views.menu

import scala.collection.mutable.LinkedHashMap
import controllers.routes

object EventMenu {
  val menu = LinkedHashMap(
  "nav.event.list" -> routes.EventController.list,
  "nav.event.calendar" -> routes.EventController.calendar,
  "nav.event.map" -> routes.EventController.map//,
  //"nav.event.grand.list" -> routes.GrandEventController.list
  );
  implicit val implicitLeftNav = menu;
}
