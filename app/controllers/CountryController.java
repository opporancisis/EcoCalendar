package controllers;

import java.util.List;

import models.geo.City;
import models.geo.Country;
import models.user.RoleName;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
@Restrict({ @Group(RoleName.ADMIN) })
public class CountryController extends Controller {

	private static final Form<CountryProps> EDIT_FORM = Form.form(CountryProps.class);

	public Result list() {
		return ok(views.html.geo.listCountries.render(Country.find.all()));
	}

	public Result edit(Country country) {
		return ok(views.html.geo.editCountry.render(EDIT_FORM.fill(new CountryProps(country)),
				country));
	}

	public Result doEdit(Country country) {
		Form<CountryProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCountry.render(filledForm, country));
		}
		CountryProps countryProps = filledForm.get();
		countryProps.updateCountry(country);
		return redirect(routes.CountryController.list());
	}

	public Result add() {
		return ok(views.html.geo.editCountry.render(EDIT_FORM, null));
	}

	public Result doAdd() {
		Form<CountryProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCountry.render(filledForm, null));
		}
		CountryProps countryProps = filledForm.get();
		countryProps.createCountry().save();
		return redirect(routes.CountryController.list());
	}

	public Result remove(Country country) {
		country.delete();
		return ok();
	}

	public static class CountryProps {
		public Boolean disabled;

		@Required
		public String name;

		/**
		 * Code is used for constructing human-readable URLs. If empty, then id
		 * is used.
		 */
		public String code;

		public List<City> cities;

		@Required
		public Double centerLatitude;

		@Required
		public Double centerLongitude;

		@Required
		public Integer defaultZoom;

		public CountryProps() {
			// no-op
		}

		public CountryProps(Country country) {
			this.disabled = country.disabled;
			this.name = country.name;
			this.code = country.code;
			this.cities = country.cities;
			this.centerLatitude = country.centerLatitude;
			this.centerLongitude = country.centerLongitude;
			this.defaultZoom = country.defaultZoom;
		}

		private void setFields(Country country) {
			country.disabled = this.disabled;
			country.name = this.name;
			country.code = this.code;
			country.cities = this.cities;
			country.centerLatitude = this.centerLatitude;
			country.centerLongitude = this.centerLongitude;
			country.defaultZoom = this.defaultZoom;
		}

		public void updateCountry(Country country) {
			setFields(country);
		}

		public Country createCountry() {
			Country country = new Country();
			setFields(country);
			return country;
		}
	}
}
