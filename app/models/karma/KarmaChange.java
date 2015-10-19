package models.karma;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.Model;

import com.avaje.ebean.annotation.CreatedTimestamp;

@Entity
public class KarmaChange extends Model {

	@Id
	public Long id;

	public KarmaChangeType changeType;

	public String customReason;

	public Long value;

	@CreatedTimestamp
	public Date created;

}
