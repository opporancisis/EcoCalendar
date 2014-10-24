package models.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import models.event.tag.EventTagsList;
import models.geo.GeoCoords;
import models.organization.Organization;
import models.organization.Organizations;
import models.user.User;

import org.hibernate.validator.constraints.URL;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.libs.Json;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Entity
public class Event extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@CreatedTimestamp
	public Date created;

	@UpdatedTimestamp
	public Date updated;

	@ManyToOne
	public User author;

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

	@ManyToMany(cascade = CascadeType.ALL)
	@EventTagsList
	public List<EventTag> tags;

	@ManyToMany(cascade = CascadeType.ALL)
	@Organizations
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
		return days.get(days.size() - 1);
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

	public String processTimetable() {
		JsonNode timetableArr = Json.parse(timetableJson);
		if (!timetableArr.isArray() || timetableArr.size() == 0) {
			return "incorrect timetable data format";
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
		processTimetable();
		update(id);
		newCoords.update(this.coords.id);

	}
}
