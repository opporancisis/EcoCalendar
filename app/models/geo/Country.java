package models.geo;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import utils.IdPathBindable;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
public class Country extends Model implements IdPathBindable<Country> {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(Country.class, new CountryFormatter());
	}

	@Id
	public Long id;

	@CreatedTimestamp
	public Date created;

	@UpdatedTimestamp
	public Date updated;

	public Boolean disabled;

	@Required
	public String name;

	/**
	 * Code is used for constructing human-readable URLs. If empty, then id is
	 * used.
	 */
	public String code;

	@OneToMany
	public List<City> cities;

	@Required
	public Double centerLatitude;

	@Required
	public Double centerLongitude;

	@Required
	public Integer defaultZoom;

	public static Finder<Long, Country> find = new Finder<>(Long.class, Country.class);

	public static List<Country> all() {
		List<Country> countries = find.all();
		for (Iterator<Country> it = countries.iterator(); it.hasNext();) {
			Country country = it.next();
			if (country.cities.isEmpty()) {
				it.remove();
			}
		}
		countries.sort(new Comparator<Country>() {
			@Override
			public int compare(Country c1, Country c2) {
				return Integer.compare(c2.cities.size(), c1.cities.size());
			}
		});
		return countries;
	}

	public static class CountryFormatter extends SimpleFormatter<Country> {
		@Override
		public Country parse(String text, Locale locale) throws ParseException {
			long id;
			try {
				id = Long.parseLong(text);
			} catch (NumberFormatException e) {
				throw new ParseException(text, 0);
			}
			return Country.find.byId(id);
		}

		@Override
		public String print(Country t, Locale locale) {
			return t.id.toString();
		}
	}

}
