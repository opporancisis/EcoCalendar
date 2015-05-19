package models.event;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import models.event.tag.EventTag;
import models.geo.City;
import models.geo.Country;
import models.geo.GeoCoords;
import models.organization.Organization;
import models.user.User;

import org.hibernate.validator.constraints.URL;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.db.ebean.Model;
import play.i18n.Messages;
import utils.IdPathBindable;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
public class Event extends Model implements IdPathBindable<Event> {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(Event.class, new EventFormatter());
	}

	@Id
	public Long id;

	@CreatedTimestamp
	public Date created;

	@UpdatedTimestamp
	public Date updated;

	public Boolean published;

	@ManyToOne
	public User author;

	@Required
	public String name;

	@Required
	@Lob
	public String description;

	@ManyToMany(mappedBy = "events", cascade = CascadeType.ALL)
	public List<EventTag> tags;

	@URL
	public String additionalInfoLink;

	public Boolean moreGeneralSettings;

	@ManyToOne
	public GrandEvent parent;

	@ManyToMany(mappedBy = "events", cascade = CascadeType.ALL)
	public List<Organization> organizations;

	public Boolean useContactInfo;

	public Boolean useAuthorContactInfo;

	public String contactName;

	public String contactPhone;

	public String contactProfile;

	@Transient
	@Required
	public Country country;

	@ManyToOne
	@Required
	public City city;

	public String address;

	public Boolean extendedGeoSettings;

	@OneToMany(cascade = CascadeType.ALL)
	public List<GeoCoords> coords;

	@Required
	public LocalDate startDate;

	@Required
	public LocalTime startTime;

	@Required
	public LocalDate endDate;

	@Required
	public LocalTime endTime;

	public static Finder<Long, Event> find = new Finder<>(Long.class, Event.class);

	public List<ValidationError> validate() {
		List<ValidationError> errors = null;
		if (parent != null) {
			if (parent.startDate.isAfter(startDate)) {
				errors = new ArrayList<>();
				errors.add(new ValidationError("", Messages.get(
						"label.event.error.parentStartDateIsAfter", parent.startDate)));
			}
			if (parent.endDate.isBefore(endDate)) {
				if (errors == null) {
					errors = new ArrayList<>();
				}
				errors.add(new ValidationError("", Messages.get(
						"label.event.error.parentEndDateIsBefore", parent.endDate)));
			}
		}
		return errors;
	}

	public static class EventFormatter extends SimpleFormatter<Event> {

		@Override
		public Event parse(String text, Locale locale) throws ParseException {
			long id;
			try {
				id = Long.parseLong(text);
			} catch (NumberFormatException e) {
				throw new ParseException(text, 0);
			}
			return Event.find.byId(id);
		}

		@Override
		public String print(Event t, Locale locale) {
			return "" + t.id;
		}
	}
}
