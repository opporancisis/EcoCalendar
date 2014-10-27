import java.util.List;
import java.util.Map;

import models.user.SecurityRole;
import models.user.User;
import play.libs.Yaml;
import providers.MyUsernamePasswordAuthUser;

import com.avaje.ebean.Ebean;

public class InitialData {

	public static void initData() {
		if (SecurityRole.find.findRowCount() > 0) {
			return;
		}

		Map<String, List<Object>> all = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
		Ebean.save(all.get("settings"));
		Ebean.save(all.get("roles"));
		Ebean.save(all.get("users"));
        for(Object user: all.get("users")) {
            // Insert the project/user relation
            Ebean.saveManyToManyAssociations(user, "roles");
        }
		Ebean.save(all.get("standardPages"));
		Ebean.save(all.get("countries"));
		Ebean.save(all.get("cities"));
		Ebean.save(all.get("organizations"));
		Ebean.save(all.get("eventTags"));

		User.find.query().where().eq("nick", "admin").findUnique()
				.changePassword(new MyUsernamePasswordAuthUser("napaAdmin150"), true);

		User.find.query().where().eq("nick", "demo").findUnique()
				.changePassword(new MyUsernamePasswordAuthUser("ecoEvent450"), true);

	}

}
