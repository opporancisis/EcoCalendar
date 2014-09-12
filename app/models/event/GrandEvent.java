package models.event;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.event.tag.EventTag;
import models.event.tag.EventTagsList;
import models.organization.Organization;
import models.organization.Organizations;
import models.user.User;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
public class GrandEvent extends Model {

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

	@Required
	public String name;

	@Required
	public String description;

	public LocalDate startDate;

	public LocalDate endDate;

	@OneToMany(mappedBy = "parent")
	public List<Event> events;

	@ManyToMany
	@EventTagsList
	public List<EventTag> tags;

	@ManyToMany
	@Organizations
	public List<Organization> organizations;

	public static Finder<Long, GrandEvent> find = new Finder<>(Long.class, GrandEvent.class);
}
