package models.event.tag;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import models.event.Event;
import models.event.GrandEvent;
import play.data.format.Formatters;
import play.db.ebean.Model;
import utils.IdPathBindable;
import utils.formatter.EventTagFormatter;

@Entity
public class EventTag extends Model implements IdPathBindable<EventTag> {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(EventTag.class, new EventTagFormatter());
	}

	@Id
	public Long id;

	public String name;

	@ManyToMany
	public List<Event> events;

	@ManyToMany
	public List<GrandEvent> grandEvents;

	public static Finder<Long, EventTag> find = new Finder<>(Long.class, EventTag.class);

	public static List<EventTag> findAllOrdered() {
		return find.query().order("name").findList();
	}
}
