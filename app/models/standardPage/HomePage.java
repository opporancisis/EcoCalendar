package models.standardPage;

import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import models.file.UploadedFile;
import models.sys.Setting;
import models.sys.SettingName;
import play.i18n.Messages;

import com.avaje.ebean.Model;

@Entity
public class HomePage extends Model {

	@Id
	public Long id;

	public String title;

	@Column(columnDefinition = "TEXT")
	public String content;

	@ManyToMany
	public List<UploadedFile> attachments;

	public Integer latestNewsMax;

	public static final Find<Long, HomePage> find = new Find<Long, HomePage>() {
	};

	public static HomePage get() {
		Iterator<HomePage> it = find.all().iterator();
		if (it.hasNext()) {
			return it.next();
		}
		HomePage home = new HomePage();
		String portalTitle = Setting.get(SettingName.PORTAL_TITLE).value;
		if (portalTitle != null) {
			home.title = Messages.get("label.welcome.to.portal", portalTitle);
		} else {
			home.title = Messages.get("label.welcome");
		}
		home.content = Messages.get("label.home.not.configured");
		home.latestNewsMax = 3;
		home.save();
		return home;
	}
}
