package models.event;

import java.text.ParseException;
import java.time.LocalDateTime;
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

import models.event.tag.EventTag;
import models.geo.City;
import models.geo.GeoCoords;
import models.organization.Organization;
import models.user.User;
import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import utils.IdPathBindable;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
public class Event extends Model implements IdPathBindable<Event> {

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

	public String name;

	@Lob
	public String description;

	@ManyToMany(mappedBy = "events", cascade = CascadeType.ALL)
	public List<EventTag> tags;

	public String additionalInfoLink;

	@ManyToOne
	public GrandEvent parent;

	@ManyToMany(mappedBy = "events", cascade = CascadeType.ALL)
	public List<Organization> organizations;

	public Boolean useContactInfo;

	public Boolean useAuthorContactInfo;

	public String contactName;

	public String contactPhone;

	public String contactProfile;

	@ManyToOne
	public City city;

	public String address;

	public Boolean extendedGeoSettings;

	@OneToMany(cascade = CascadeType.ALL)
	public List<GeoCoords> coords;

	public LocalDateTime start;

	public LocalDateTime finish;

	public static Find<Long, Event> find = new Find<Long, Event>() {
	};

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
