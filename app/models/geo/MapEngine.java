package models.geo;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class MapEngine extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String name;

	public String url;

	public static Finder<Long, MapEngine> find = new Finder<>(Long.class, MapEngine.class);
}
