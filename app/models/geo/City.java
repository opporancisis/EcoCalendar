package models.geo;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class City extends Model {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(City.class, new CityFormatter());
	}

	@Id
	public Long id;

	@Required
	public String name;

	@ManyToOne
	@JsonIgnore
	@Required
	public Country country;

	@JsonIgnore
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
