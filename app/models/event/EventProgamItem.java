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
	 * Minutes from 00:00 of the event day.
	 */
	public LocalTime start;

	/**
	 * Minutes from 00:00 of the event day.
	 */
	public LocalTime end;

	public String description;

	@ManyToOne
	public EventDayProgram program;
}
