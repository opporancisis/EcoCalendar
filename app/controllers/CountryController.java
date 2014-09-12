package controllers;

import models.geo.Country;
import models.geo.MapEngine;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class CountryController extends Controller {

	private static final Form<Country> EDIT_FORM = Form.form(Country.class);

	public static Result list() {
		return ok(views.html.geo.listCountries.render(Country.find.all()));
	}

	public static Result edit(long id) {
		Country country = Country.find.byId(id);
		if (country == null) {
			return Application.notFoundObject(Country.class, id);
		}
		return ok(views.html.geo.editCountry.render(EDIT_FORM.fill(country), country,
				MapEngine.find.all()));
	}

	public static Result doEdit(long id) {
		Country country = Country.find.byId(id);
		if (country == null) {
			return Application.notFoundObject(Country.class, id);
		}
		Form<Country> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCountry.render(filledForm, country,
					MapEngine.find.all()));
		}
		Country updatedCountry = filledForm.get();
		updatedCountry.update(id);
		return redirect(routes.CountryController.list());
	}

	public static Result create() {
		return ok(views.html.geo.editCountry.render(EDIT_FORM, null, MapEngine.find.all()));
	}

	public static Result doCreate() {
		Form<Country> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCountry.render(filledForm, null,
					MapEngine.find.all()));
		}
		Country country = filledForm.get();
		country.save();
		return redirect(routes.CountryController.list());
	}

	public static Result remove(long id) {
		Country country = Country.find.byId(id);
		if (country == null) {
			return Application.notFoundObject(Country.class, id);
		}
		country.delete();
		return ok();
	}

}
