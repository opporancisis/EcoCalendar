package models.blog;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.file.UploadedFile;
import models.user.User;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;
import utils.IdPathBindable;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
public class BlogPost extends Model implements IdPathBindable<BlogPost> {

	@Id
	public Long id;

	@CreatedTimestamp
	public Date created;

	@UpdatedTimestamp
	public Date updated;

	@Required
	public String title;

	@Required
	@Column(columnDefinition = "TEXT")
	public String content;

	@ManyToMany(cascade = CascadeType.ALL)
	public List<UploadedFile> attachments;

	@ManyToOne
	public User owner;

	public static final Find<Long, BlogPost> find = new Find<Long, BlogPost>() {
	};

}
