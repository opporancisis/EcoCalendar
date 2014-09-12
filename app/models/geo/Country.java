package models.geo;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class Country extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String name;

	@OneToMany
	public List<City> cities;

	public Double centerLatitude;

	public Double centerLongitude;

	public Integer defaultZoom;

	@ManyToOne
	public MapEngine defaultMapEngine;

	public static Finder<Long, Country> find = new Finder<>(Long.class, Country.class);
}
