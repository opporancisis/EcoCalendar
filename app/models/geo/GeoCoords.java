package models.geo;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

@Entity
public class GeoCoords extends Model {

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

	public String comment;

	// @ManyToOne
	// @Required
	// public City city;
	//
	// public String address;
	//
	// @Transient
	// @Required
	// public Country country;

	public static Finder<Long, GeoCoords> find = new Finder<>(Long.class, GeoCoords.class);

}
