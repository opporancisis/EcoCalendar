package controllers;

import java.time.ZoneId;

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
public class CityController extends Controller {

	private static final Form<CityProps> EDIT_FORM = Form.form(CityProps.class);

	public Result list() {
		return ok(views.html.geo.listCities.render(City.find.all()));
	}

	public Result edit(City city) {
		return ok(views.html.geo.editCity.render(EDIT_FORM.fill(new CityProps(city)), city,
				Country.find.all()));
	}

	public Result doEdit(City city) {
		Form<CityProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCity.render(filledForm, city, Country.find.all()));
		}
		CityProps cityProps = filledForm.get();
		cityProps.updateCity(city);
		return redirect(routes.CityController.list());
	}

	public Result add() {
		return ok(views.html.geo.editCity.render(EDIT_FORM, null, Country.find.all()));
	}

	public Result doAdd() {
		Form<CityProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editCity.render(filledForm, null, Country.find.all()));
		}
		CityProps cityProps = filledForm.get();
		cityProps.createCity().save();
		return redirect(routes.CityController.list());
	}

	public Result remove(City city) {
		city.delete();
		return ok();
	}

	public static class CityProps {
		public Boolean disabled;

		@Required
		public String name;

		/**
		 * Code is used for constructing human-readable URLs. If empty, then id
		 * is used.
		 */
		public String code;

		@Required
		public Country country;

		public ZoneId zone;

		@Required
		public Double centerLatitude;

		@Required
		public Double centerLongitude;

		@Required
		public Integer defaultZoom;

		/**
		 * The bigger is weight - higher city is placed in lists
		 */
		@Required
		public Integer weight;

		public CityProps() {
			// no-op
		}

		public CityProps(City city) {
			this.disabled = city.disabled;
			this.name = city.name;
			this.code = city.code;
			this.country = city.country;
			this.zone = city.zone;
			this.centerLatitude = city.centerLatitude;
			this.centerLongitude = city.centerLongitude;
			this.defaultZoom = city.defaultZoom;
			this.weight = city.weight;
		}

		private void setFields(City city) {
			city.disabled = this.disabled;
			city.name = this.name;
			city.code = this.code;
			city.country = this.country;
			city.zone = this.zone;
			city.centerLatitude = this.centerLatitude;
			city.centerLongitude = this.centerLongitude;
			city.defaultZoom = this.defaultZoom;
			city.weight = this.weight;
		}

		public void updateCity(City city) {
			setFields(city);
			city.update();
		}

		public City createCity() {
			City city = new City();
			setFields(city);
			return city;
		}
	}

}
