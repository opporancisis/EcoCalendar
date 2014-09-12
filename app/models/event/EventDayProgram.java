package models.event;

import java.time.LocalDate;
import java.util.List;

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

	@OneToMany
	public List<EventProgamItem> items;

}
