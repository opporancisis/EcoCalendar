package models.organization.tag;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import models.organization.Organization;
import play.data.format.Formatters;
import play.db.ebean.Model;

@Entity
public class OrganizationTag extends Model {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(OrganizationTag.class, new OrganizationTagFormatter());
		Formatters.register(List.class, new OrganizationTagsListAnnotationFormatter());
	}

	@Id
	public Long id;

	public String name;

	@ManyToMany
	public List<Organization> organizations;

	public static Finder<Long, OrganizationTag> find = new Finder<>(Long.class,
			OrganizationTag.class);

}
