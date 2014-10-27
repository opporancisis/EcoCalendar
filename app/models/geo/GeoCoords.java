package models.geo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class GeoCoords extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	/**
	 * Широта.
	 */
	@Required
	public Double latitude;

	/**
	 * Долгота.
	 */
	@Required
	public Double longitude;

	@ManyToOne
	@Required
	public City city;

	public String address;

	@Transient
	@Required
	public Country country;

	public static Finder<Long, GeoCoords> find = new Finder<>(Long.class, GeoCoords.class);

	// TODO: do we need such validation? city and country already have @Required
	// public List<ValidationError> validate() {
	// List<ValidationError> errors = new ArrayList<ValidationError>();
	// if (country == null) {
	// errors.add(new ValidationError("country",
	// Messages.get("error.required")));
	// }
	// if (city == null) {
	// errors.add(new ValidationError("city", Messages.get("error.required")));
	// }
	// return errors.isEmpty() ? null : errors;
	// }

}
