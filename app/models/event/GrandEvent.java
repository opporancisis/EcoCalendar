package models.event;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.event.tag.EventTag;
import models.organization.Organization;
import models.user.User;
import play.data.format.Formatters;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import utils.formatter.GrandEventFormatter;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
public class GrandEvent extends Model {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(GrandEvent.class, new GrandEventFormatter());
	}

	@Id
	public Long id;

	@CreatedTimestamp
	public Date created;

	@UpdatedTimestamp
	public Date updated;

	@ManyToOne
	public User author;

	public Boolean published;

	// TODO: make this logic work
	/**
	 * If it's true, then notify GE-creator about each new sub event and don't
	 * enable sub-event until premoderation
	 */
	public Boolean preModeration;

	@Required
	public String name;

	@Required
	@Lob
	public String description;

	/**
	 * Strict time border for all sub-events. No sub-event can start before
	 * startDate.
	 */
	public LocalDate startDate;

	/**
	 * Strict time border for all sub-events. No sub-event can finish after
	 * endDate.
	 */
	public LocalDate endDate;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
	public List<Event> events;

	@ManyToMany(mappedBy = "grandEvents")
	public List<EventTag> tags;

	@ManyToMany(mappedBy = "grandEvents")
	public List<Organization> organizations;

	public static Finder<Long, GrandEvent> find = new Finder<>(Long.class, GrandEvent.class);
}
