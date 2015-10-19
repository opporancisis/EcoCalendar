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
import com.avaje.ebean.Model;
import utils.IdPathBindable;
import utils.formatter.OrganizationFormatter;

@Entity
public class Organization extends Model implements IdPathBindable<Organization> {

	static {
		Formatters.register(Organization.class, new OrganizationFormatter());
	}

	@Id
	public Long id;

	public String name;

	@Lob
	public String description;

	@ManyToMany
	public List<Event> events;

	@ManyToMany
	public List<GrandEvent> grandEvents;

	public static final Find<Long, Organization> find = new Find<Long, Organization>() {
	};

	// TODO:
	// logo of organization
}
