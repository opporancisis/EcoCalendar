package models.geo;

import java.time.ZoneId;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class City extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String name;

	@ManyToOne
	public Country country;

	public ZoneId zone;

	public Double centerLatitude;

	public Double centerLongitude;

	public Integer defaultZoom;

	@ManyToOne
	public MapEngine defaultMapEngine;

	public static Finder<Long, City> find = new Finder<>(Long.class, City.class);

}
