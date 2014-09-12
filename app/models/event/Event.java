package models.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import models.event.tag.EventTag;
import models.event.tag.EventTagsList;
import models.geo.GeoCoords;
import models.organization.Organization;
import models.organization.Organizations;
import models.user.User;

import org.hibernate.validator.constraints.URL;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

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

	@OneToOne
	public GeoCoords coords;

	@Required
	public String name;

	@Required
	public String description;

	@URL
	public String additionalInfoLink;

	@URL
	public String postReleaseLink;

	@OneToMany
	public List<EventDayProgram> days;

	@ManyToMany
	@EventTagsList
	public List<EventTag> tags;

	@ManyToMany
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
}
