package views.menu

import scala.collection.mutable.LinkedHashMap
import controllers.routes

object SystemMenu {
  implicit val implicitLeftNav = LinkedHashMap(
	"nav.sys.settings" -> routes.SettingController.edit,
	"nav.homePage.edit" -> routes.HomePageController.edit,
	"nav.user.list" -> routes.UserController.list,
	"nav.file.list" -> routes.FileController.list,
	"nav.stdPage.list" -> routes.StandardPageController.list,
	"nav.event.tag.list" -> routes.EventTagController.list,
	"nav.organization.list" -> routes.OrganizationController.list,
	"nav.geo.country.list" -> routes.CountryController.list,
	"nav.geo.city.list" -> routes.CityController.list
  )
}
