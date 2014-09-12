import java.util.ArrayList;

import models.standardPage.StandardPage;
import models.sys.Setting;
import models.sys.SettingName;
import models.user.RoleName;
import models.user.SecurityRole;
import models.user.User;
import play.i18n.Messages;
import providers.MyUsernamePasswordAuthUser;

public class InitialData {

	public static void initData() {
		if (SecurityRole.find.findRowCount() > 0) {
			return;
		}
		for (SettingName name : SettingName.values()) {
			if (name.getDefaultVal() != null) {
				Setting.update(name, name.getDefaultVal());
			}
		}
		SecurityRole adminRole = null;
		SecurityRole userRole = null;
		for (final String roleName : RoleName.asList()) {
			final SecurityRole role = new SecurityRole();
			role.roleName = roleName;
			role.save();
			switch (roleName) {
			case RoleName.ADMIN:
				adminRole = role;
				break;
			case RoleName.USER:
				userRole = role;
				break;
			}
		}

		User deletedAccount = new User();
		deletedAccount.roles = new ArrayList<>();
		deletedAccount.nick = Messages.get("label.deleted.user.nick");
		deletedAccount.emailValidated = false;
		deletedAccount.blocked = true;
		deletedAccount.save();
		Setting.update(SettingName.DELETED_USER_ID, deletedAccount.id);

		User admin = new User();
		admin.roles = new ArrayList<>();
		admin.roles.add(adminRole);
		admin.nick = "admin";
		admin.email = "admin@ecocalendar.ru";
		admin.emailValidated = true;
		admin.blocked = false;
		admin.save();
		admin.changePassword(new MyUsernamePasswordAuthUser("ecoAdmin150"), true);

		User defaultUser = new User();
		defaultUser.roles = new ArrayList<>();
		defaultUser.roles.add(userRole);
		defaultUser.nick = "defaultUser";
		defaultUser.email = "defaultUser@ecocalendar.ru";
		defaultUser.emailValidated = true;
		defaultUser.blocked = false;
		defaultUser.save();
		defaultUser.changePassword(new MyUsernamePasswordAuthUser("ecoEvent450"), true);

		String[] stdPages = new String[] { "О Портале", "Как помочь проекту", "Наша команда" };
		for (int i = 0; i < stdPages.length; i++) {
			StandardPage.newPage(i + 1, stdPages[i]);
		}
	}

}
