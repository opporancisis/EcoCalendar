package ru.ecocalendar;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import models.user.SecurityRole;
import models.user.User;
import play.Logger;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;

public class InitialData {

	public static void initData(String file) {
		if (SecurityRole.find.findRowCount() > 0) {
			return;
		}

		Map<String, List<Object>> all = load(file);
		save(all, "settings");
		save(all, "roles");
		save(all, "users");
		for (Object user : all.get("users")) {
			// Insert the project/user relation
			Ebean.saveManyToManyAssociations(user, "roles");
		}
		save(all, "standardPages");
		save(all, "countries");
		save(all, "cities");
		save(all, "organizations");
		save(all, "eventTags");

		initPassword("root@localhost", "ecoAdmin150");
	}

	public static void initPassword(String login, String password) {
		User user = User.find.query().where().eq("email", login).findUnique();
		if (user != null) {
			user.changePassword(password, true);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<Object>> load(String file) {
		Logger.info("initial data setup with file " + file);
		InputStream is = InitialData.class.getClassLoader().getResourceAsStream(file);
		return (Map<String, List<Object>>) Yaml.load(is, InitialData.class.getClassLoader());
	}

	public static List<Object> save(Map<String, List<Object>> data, String key) {
		List<Object> list = data.get(key);
		if (list != null) {
			Ebean.saveAll(list);
		}
		return list;
	}
}
