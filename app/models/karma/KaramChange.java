package models.karma;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.avaje.ebean.annotation.CreatedTimestamp;

@Entity
public class KaramChange extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public KarmaChangeType changeType;

	public String customReason;

	public Long value;

	@CreatedTimestamp
	public Date created;

}
