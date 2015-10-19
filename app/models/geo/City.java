package models.geo;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import com.avaje.ebean.Model;
import utils.IdPathBindable;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class City extends Model implements IdPathBindable<City> {

	static {
		Formatters.register(City.class, new CityFormatter());
	}

	@Id
	public Long id;

	@CreatedTimestamp
	public Date created;

	@UpdatedTimestamp
	public Date updated;

	public Boolean disabled;

	public String name;

	/**
	 * Code is used for constructing human-readable URLs. If empty, then id is
	 * used.
	 */
	public String code;

	@ManyToOne
	@JsonIgnore
	public Country country;

	@JsonIgnore
	public ZoneId zone;

	public Double centerLatitude;

	public Double centerLongitude;

	public Integer defaultZoom;

	/**
	 * The bigger is weight - higher city is placed in lists
	 */
	public Integer weight;

	public static Finder<Long, City> find = new Finder<>(Long.class, City.class);

	public static class CityFormatter extends SimpleFormatter<City> {
		@Override
		public City parse(String text, Locale locale) throws ParseException {
			long id;
			try {
				id = Long.parseLong(text);
			} catch (NumberFormatException e) {
				throw new ParseException(text, 0);
			}
			return City.find.byId(id);
		}

		@Override
		public String print(City t, Locale locale) {
			return t.id.toString();
		}
	}

}
