package models.standardPage;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import models.file.UploadedFile;
import models.sys.Setting;
import models.sys.SettingName;
import play.data.validation.Constraints.Min;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.i18n.Messages;

@Entity
public class HomePage extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Required
	public String title;

	@Column(columnDefinition = "TEXT")
	public String body;

	@ManyToMany
	public List<UploadedFile> attachments;

	@Min(0)
	public Integer latestNewsMax;

	public static final Finder<Long, HomePage> find = new Finder<>(Long.class,
			HomePage.class);

	public static HomePage get() {
		HomePage home = find.findUnique();
		if (home == null) {
			home = new HomePage();
			String portalTitle = Setting.get(SettingName.PORTAL_TITLE).value;
			if(portalTitle != null) {
				home.title = Messages.get("label.welcome.to.portal", portalTitle);
			} else {
				home.title = Messages.get("label.welcome");
			}
			home.body = Messages.get("label.home.not.configured");
			home.latestNewsMax = 3;
			home.save();
		}
		return home;
	}
}
