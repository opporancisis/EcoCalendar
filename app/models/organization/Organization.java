package models.organization;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

import models.event.Event;
import models.event.GrandEvent;
import play.data.format.Formatters;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import utils.formatter.OrganizationFormatter;

@Entity
public class Organization extends Model {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(Organization.class, new OrganizationFormatter());
	}

	@Id
	public Long id;

	@Required
	public String name;

	// TODO: make rich-edit control for description
	@Required
	@Lob
	public String description;

	@ManyToMany
	public List<Event> events;

	@ManyToMany
	public List<GrandEvent> grandEvents;

	public static Finder<Long, Organization> find = new Finder<>(Long.class, Organization.class);

	// TODO:
	// logo of organization
}
