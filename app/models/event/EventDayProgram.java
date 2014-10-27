package models.event;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class EventDayProgram extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public LocalDate date;

	@ManyToOne
	public Event event;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "program")
	public List<EventProgamItem> items;

	public EventProgamItem lastItem() {
		EventProgamItem epi = null;
		for (EventProgamItem item : items) {
			if (epi == null) {
				epi = item;
				continue;
			}
			if (epi.end.isBefore(item.end)) {
				epi = item;
			}
		}
		return epi;
	}

	public EventProgamItem firstItem() {
		EventProgamItem epi = null;
		for (EventProgamItem item : items) {
			if (epi == null) {
				epi = item;
				continue;
			}
			if (epi.start.isAfter(item.start)) {
				epi = item;
			}
		}
		return epi;
	}
}
