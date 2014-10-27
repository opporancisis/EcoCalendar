package models.event;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import models.event.tag.EventTag;
import models.geo.GeoCoords;
import models.organization.Organization;
import models.user.User;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.db.ebean.Model;
import play.i18n.Messages;
import play.libs.Json;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Entity
public class Event extends Model {

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

	@ManyToOne
	public User author;

	public Boolean useAuthorNameAndPhone;

	public String contactName;

	public String contactPhone;

	public Boolean published;

	@ManyToOne
	public GrandEvent parent;

	@OneToOne(cascade = CascadeType.ALL)
	public GeoCoords coords;

	@Required
	public String name;

	@Required
	public String description;

	@URL
	public String additionalInfoLink;

	@URL
	public String postReleaseLink;

	@Transient
	public String timetableJson;

	@OneToMany(cascade = CascadeType.ALL)
	public List<EventDayProgram> days;

	@ManyToMany(mappedBy = "events")
	public List<EventTag> tags;

	@ManyToMany(mappedBy = "events")
	public List<Organization> organizations;

	public static Finder<Long, Event> find = new Finder<>(Long.class, Event.class);

	public boolean finished() {
		EventDayProgram lastDay = lastDay();
		if (lastDay.date.isBefore(LocalDate.now())) {
			return true;
		}
		return lastDay.items.get(lastDay.items.size() - 1).end.isBefore(LocalTime.now());
	}

	public EventDayProgram lastDay() {
		EventDayProgram edp = null;
		for (EventDayProgram day : days) {
			if (edp == null) {
				edp = day;
				continue;
			}
			if (edp.date.isBefore(day.date)) {
				edp = day;
			}
		}
		return edp;
	}

	public EventDayProgram firstDay() {
		EventDayProgram edp = null;
		for (EventDayProgram day : days) {
			if (edp == null) {
				edp = day;
				continue;
			}
			if (edp.date.isAfter(day.date)) {
				edp = day;
			}
		}
		return edp;
	}

	public void initTimetableJson() {
		ArrayNode arr = Json.newObject().arrayNode();
		for (EventDayProgram day : days) {
			LocalDate date = day.date;
			for (EventProgamItem item : day.items) {
				arr.add(Json.newObject()
						.put("start", LocalDateTime.of(date, item.start).toString())
						.put("end", LocalDateTime.of(date, item.end).toString())
						.put("title", item.description));
			}
		}
		timetableJson = arr.toString();
	}

	public List<ValidationError> validate() {
		List<ValidationError> errors = processTimetable();
		if (errors != null) {
			return errors;
		}
		if (parent != null) {
			if (parent.startDate.isAfter(firstDay().date)) {
				errors = new ArrayList<>();
				errors.add(new ValidationError("", Messages.get(
						"label.event.error.parentStartDateIsAfter", parent.startDate)));
			}
			if (parent.endDate.isBefore(lastDay().date)) {
				if (errors == null) {
					errors = new ArrayList<>();
				}
				errors.add(new ValidationError("", Messages.get(
						"label.event.error.parentEndDateIsBefore", parent.endDate)));
			}
		}
		return errors;
	}

	private List<ValidationError> processTimetable() {
		JsonNode timetableArr = Json.parse(timetableJson);
		if (!timetableArr.isArray() || timetableArr.size() == 0) {
			return Arrays.<ValidationError> asList(new ValidationError("", Messages
					.get("error.incorrect.timetable.data.format")));
		}
		Map<LocalDate, EventDayProgram> items = new HashMap<>();
		for (JsonNode elem : timetableArr) {
			LocalDateTime startLdt = LocalDateTime.parse(elem.get("start").asText());
			LocalDateTime endLdt = LocalDateTime.parse(elem.get("end").asText());
			String title = elem.get("title").asText();
			LocalDate ld = startLdt.toLocalDate();
			EventDayProgram dayProgram = items.get(ld);
			if (dayProgram == null) {
				dayProgram = new EventDayProgram();
				dayProgram.date = ld;
				items.put(ld, dayProgram);
				dayProgram.items = new ArrayList<>();
			}
			dayProgram.items.add(new EventProgamItem(startLdt.toLocalTime(), endLdt.toLocalTime(),
					title, dayProgram));
		}
		this.days = new ArrayList<>(items.values());
		return null;
	}

	public void updateWithTimetableAndCoords(long id, GeoCoords newCoords) {
		update(id);
		newCoords.update(this.coords.id);
	}

	public String contact() {
		if (BooleanUtils.isTrue(useAuthorNameAndPhone)) {
			if (StringUtils.isBlank(author.phone)) {
				return null;
			}
			if (StringUtils.isNotBlank(author.name)) {
				return author.name + ", " + author.phone;
			} else {
				return author.phone;
			}
		} else {
			if (StringUtils.isBlank(contactPhone)) {
				return null;
			}
			if (StringUtils.isNotBlank(contactName)) {
				return contactName + ", " + contactPhone;
			} else {
				return contactPhone;
			}
		}
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
