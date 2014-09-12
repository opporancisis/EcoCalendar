package models.sys;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;

import play.data.format.Formatters;
import play.db.ebean.Model;
import play.i18n.Messages;

@Entity
public class Setting extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public Class<?> clazz;

	public String name;

	@Column(columnDefinition = "TEXT")
	public String value;

	/**
	 * If editable, then this setting may be changed in System -&gt; portal
	 * settings UI interface
	 */
	public Boolean editable;

	public static final Finder<Long, Setting> find = new Finder<>(Long.class,
			Setting.class);

	public String validate() {
		try {
			Formatters.parse(value, clazz);
			return null;
		} catch (Exception e) {
			return Messages.get("error.invalid");
		}
	}

	public static Map<String, Setting> editableSettings() {
		return Setting.find.query().where().eq("editable", true)
				.findMap("name", String.class);
	}

	public static Long getLong(SettingName name) {
		String val = get(name).value;
		return val != null ? Long.parseLong(val) : null;
	}

	public static Setting get(SettingName name) {
		Setting setting = find.query().where().eq("name", name.name())
				.findUnique();
		if (setting == null) {
			setting = new Setting();
			setting.clazz = name.getClazz();
			setting.name = name.name();
		}
		return setting;
	}

	public static void update(SettingName name, Long value) {
		update(name, "" + value);
	}

	public static void update(SettingName name, String value) {
		Setting setting = find.query().where().eq("name", name.name())
				.findUnique();
		if (setting == null) {
			setting = new Setting();
			setting.clazz = name.getClazz();
			setting.name = name.name();
			setting.value = value;
			setting.editable = name.isEditable();
			setting.save();
		} else if (!StringUtils.equals(setting.value, value)) {
			setting.value = value;
			setting.update();
		}
	}
}