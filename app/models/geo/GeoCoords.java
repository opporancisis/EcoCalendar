package models.geo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class GeoCoords extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	/**
	 * Широта.
	 */
	public Double latitude;

	/**
	 * Долгота.
	 */
	public Double longitude;

	@ManyToOne
	public City city;

	public String address;

	public static Finder<Long, GeoCoords> find = new Finder<>(Long.class, GeoCoords.class);
}
