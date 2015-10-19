package models.standardPage;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import models.file.UploadedFile;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.avaje.ebean.Model;
import com.google.common.base.Strings;

import utils.IdPathBindable;
import controllers.routes;

@Entity
public class StandardPage extends Model implements IdPathBindable<StandardPage> {

	@Id
	public Long id;

	public Long orderInd;

	public Boolean disabled;

	public String title;

	public String link;

	@Column(columnDefinition = "TEXT")
	public String content;

	@ManyToMany(cascade = CascadeType.ALL)
	public Set<UploadedFile> attachments;

	public static final Find<Long, StandardPage> find = new Find<Long, StandardPage>() {
	};

	public String reallink() {
		return Strings.isNullOrEmpty(link) ? routes.StandardPageController.getStdPage(this)
				.toString() : link;
	}

}
