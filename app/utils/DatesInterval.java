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
		till = from.plusDays(7);
	}

	public DatesInterval(LocalDate from, LocalDate till) {
		this.from = from;
		this.till = till;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((till == null) ? 0 : till.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatesInterval other = (DatesInterval) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (till == null) {
			if (other.till != null)
				return false;
		} else if (!till.equals(other.till))
			return false;
		return true;
	}

}