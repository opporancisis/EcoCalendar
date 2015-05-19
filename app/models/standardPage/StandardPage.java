package models.standardPage;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import models.file.UploadedFile;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import utils.IdPathBindable;
import controllers.routes;

@Entity
public class StandardPage extends Model implements IdPathBindable<StandardPage> {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public Long orderInd;

	public Boolean disabled;

	@Required
	public String title;

	public String link;

	@Column(columnDefinition = "TEXT")
	public String description;

	@ManyToMany
	public List<UploadedFile> attachments;

	public static final Finder<Long, StandardPage> find = new Finder<>(
			Long.class, StandardPage.class);

	public String reallink() {
		return StringUtils.isBlank(link) ? routes.StandardPageController
				.getStdPageById(this).toString() : link;
	}

}
