package models.karma;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class KarmaHistory extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@OneToMany(cascade = CascadeType.ALL)
	public List<KaramChange> changes;

	@OneToOne
	public Karma karma;

}
