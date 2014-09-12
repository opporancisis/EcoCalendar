package utils;

import static play.data.Form.form;

import java.time.LocalDate;

import play.data.Form;
import play.data.validation.Constraints.Required;

public class DatesInterval {
	@Required
	public LocalDate from;

	@Required
	public LocalDate till;

	public static final Form<DatesInterval> FORM = form(DatesInterval.class);

	public DatesInterval() {
		from = LocalDate.now();
		from = from.minusDays(7);
		till = LocalDate.now();
	}

	public static Form<DatesInterval> fill(DatesInterval value) {
		return FORM.fill(value);
	}

	public static Form<DatesInterval> bindFromRequest(String... allowedFields) {
		return FORM.bindFromRequest(allowedFields);
	}

	public String validate() {
		if (till.isBefore(from)) {
			// return Messages
			// .get("playauthenticate.change_password.error.passwords_not_same");
			return "bad dates interval";
		}
		return null;
	}
}