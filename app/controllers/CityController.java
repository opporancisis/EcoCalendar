package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.geo.City;
import models.geo.Country;
import models.user.RoleName;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
@Restrict({ @Group(RoleName.ADMIN) })
public class CityController extends Controller {

	private static final Form<City> EDIT_FORM = Form.form(City.class);

	public static Result list() {
		return ok(views.html.geo.listCities.render(City.find.all()));
	}

	public static Result edit(City city) {
		return ok(views.html.geo.editCity.render(EDIT_FORM.fill(city), city, Country.find.all()));
	}

	public static Result doEdit(City oldCity) {
		Form<City> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCity.render(filledForm, oldCity, Country.find.all()));
		}
		City city = filledForm.get();
		city.update(oldCity.id);
		return redirect(routes.CityController.list());
	}

	public static Result add() {
		return ok(views.html.geo.editCity.render(EDIT_FORM, null, Country.find.all()));
	}

	public static Result doAdd() {
		Form<City> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCity.render(filledForm, null, Country.find.all()));
		}
		City city = filledForm.get();
		city.save();
		return redirect(routes.CityController.list());
	}

	public static Result remove(City city) {
		city.delete();
		return ok();
	}

}
