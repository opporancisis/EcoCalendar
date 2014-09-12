package models.organization;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import models.event.Event;
import models.event.GrandEvent;
import models.organization.tag.OrganizationTag;
import models.organization.tag.OrganizationTags;
import play.data.format.Formatters;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Organization extends Model {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(Organization.class, new OrganizationFormatter());
		Formatters.register(List.class, new OrganizationsListAnnotationFormatter());
	}

	@Id
	public Long id;

	@Required
	public String name;

	@Required
	public String description;

	@ManyToMany
	@OrganizationTags
	public List<OrganizationTag> tags;

	@ManyToMany
	public List<Event> events;

	@ManyToMany
	public List<GrandEvent> grandEvents;

	public static Finder<Long, Organization> find = new Finder<>(Long.class, Organization.class);

	// TODO:
	// logo of organization
}
