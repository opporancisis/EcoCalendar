package controllers;

import java.util.Map;

import models.sys.Setting;
import models.sys.SettingName;
import models.user.RoleName;

import org.apache.commons.lang3.StringUtils;

import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
@Restrict(@Group(RoleName.ADMIN))
public class SettingController extends Controller {

	public Result edit() {
		return ok(views.html.setting.editSettings.render(Setting
				.editableSettings()));
	}

	@Transactional
	public Result doEdit() {
		DynamicForm requestData = Form.form().bindFromRequest();
		Map<String, Setting> settings = Setting.editableSettings();
		for (SettingName name : SettingName.values()) {
			if (!name.isEditable()) {
				continue;
			}
			String newVal = requestData.get(name.name());
			Setting setting = settings.get(name.name());
			if (StringUtils.isEmpty(newVal)) {
				if (setting != null && StringUtils.isNotEmpty(setting.value)) {
					setting.value = newVal;
					if (validate(setting)) {
						setting.update();
					} else {
						return badRequest(views.html.setting.editSettings
								.render(Setting.editableSettings()));
					}
				}
			} else {
				if (setting == null) {
					setting = new Setting();
					setting.clazz = name.getClazz();
					setting.name = name.name();
					setting.value = newVal;
					setting.editable = name.isEditable();
					if (validate(setting)) {
						setting.save();
					} else {
						return badRequest(views.html.setting.editSettings
								.render(Setting.editableSettings()));
					}
				} else {
					setting.value = newVal;
					if (validate(setting)) {
						setting.update();
					} else {
						return badRequest(views.html.setting.editSettings
								.render(Setting.editableSettings()));
					}
				}
			}
		}
		flash(Application.FLASH_MESSAGE_KEY,
				Messages.get("label.settings.have.been.saved"));
		return redirect(routes.SettingController.edit());
	}

	private static boolean validate(Setting setting) {
		String error = setting.validate();
		if (error != null) {
			flash(Application.FLASH_ERROR_KEY, error);
			return false;
		}
		return true;
	}
}
