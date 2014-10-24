package models.event;

import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class EventProgamItem extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	/**
	 * Timestamp in seconds.
	 */
	public LocalTime start;

	/**
	 * Timestamp in seconds.
	 */
	public LocalTime end;

	public String description;

	@ManyToOne
	public EventDayProgram program;

	public EventProgamItem(LocalTime start, LocalTime end, String description,
			EventDayProgram program) {
		this.start = start;
		this.end = end;
		this.description = description;
		this.program = program;
	}

}
