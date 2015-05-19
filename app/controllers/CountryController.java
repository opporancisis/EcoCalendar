package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.geo.Country;
import models.user.RoleName;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
@Restrict({ @Group(RoleName.ADMIN) })
public class CountryController extends Controller {

	private static final Form<Country> EDIT_FORM = Form.form(Country.class);

	public static Result list() {
		return ok(views.html.geo.listCountries.render(Country.find.all()));
	}

	public static Result edit(Country country) {
		return ok(views.html.geo.editCountry.render(EDIT_FORM.fill(country), country));
	}

	public static Result doEdit(Country oldCountry) {
		Form<Country> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCountry.render(filledForm, oldCountry));
		}
		Country country = filledForm.get();
		country.update(oldCountry.id);
		return redirect(routes.CountryController.list());
	}

	public static Result add() {
		return ok(views.html.geo.editCountry.render(EDIT_FORM, null));
	}

	public static Result doAdd() {
		Form<Country> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCountry.render(filledForm, null));
		}
		Country country = filledForm.get();
		country.save();
		return redirect(routes.CountryController.list());
	}

	public static Result remove(Country country) {
		country.delete();
		return ok();
	}

}
