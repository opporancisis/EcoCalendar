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
import utils.IdPathBindable;
import utils.formatter.GrandEventFormatter;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class GrandEvent extends Model implements IdPathBindable<GrandEvent> {

	static {
		Formatters.register(GrandEvent.class, new GrandEventFormatter());
	}

	@Id
	public Long id;

	@CreatedTimestamp
	@JsonIgnore
	public Date created;

	@UpdatedTimestamp
	@JsonIgnore
	public Date updated;

	@ManyToOne
	@JsonIgnore
	public User author;

	@JsonIgnore
	public Boolean published;

	// TODO: make this logic work
	/**
	 * If it's true, then notify GE-creator about each new sub event and don't
	 * enable sub-event until premoderation
	 */
	@JsonIgnore
	public Boolean preModeration;

	public String name;

	@Lob
	@JsonIgnore
	public String description;

	/**
	 * Strict time border for all sub-events. No sub-event can start before
	 * startDate.
	 */
	@JsonIgnore
	public LocalDate startDate;

	/**
	 * Strict time border for all sub-events. No sub-event can finish after
	 * endDate.
	 */
	@JsonIgnore
	public LocalDate endDate;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
	public List<Event> events;

	@JsonIgnore
	@ManyToMany(mappedBy = "grandEvents")
	public List<EventTag> tags;

	@JsonIgnore
	@ManyToMany(mappedBy = "grandEvents")
	public List<Organization> organizations;

	public static Finder<Long, GrandEvent> find = new Finder<>(Long.class, GrandEvent.class);
}
