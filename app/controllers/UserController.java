package controllers;

import static play.data.Form.form;
import models.sys.Setting;
import models.sys.SettingName;
import models.user.RoleName;
import models.user.User;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.data.format.Formatters;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthUser;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class UserController extends Controller {

	private static final ALogger log = Logger.of(UserController.class);

	private static final Form<User> EDIT_FORM = form(User.class);

	@Restrict(@Group(RoleName.ADMIN))
	public static Result list() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(views.html.user.listUsers.render(User.find.query().where()
				.ne("id", Setting.getLong(SettingName.DELETED_USER_ID)).findList()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result create() {
		return ok(views.html.user.editUser.render(EDIT_FORM));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result doCreate() {
		final Form<User> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.user.editUser.render(filledForm));
		}
		User user = filledForm.get();
		user.save();
		return redirect(routes.UserController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result edit(long id) {
		User user = User.find.byId(id);
		if (user == null) {
			return Application.notFoundObject(User.class, id);
		}
		Form<User> filledForm = EDIT_FORM.fill(user);
		filledForm.data().put("originalName", user.somename());
		return ok(views.html.user.editUser.render(filledForm));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result doEdit(long id) {
		User oldUser = User.find.byId(id);
		if (oldUser == null) {
			return Application.notFoundObject(User.class, id);
		}
		Form<User> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			filledForm.data().put("id", Long.toString(id));
			filledForm.data().put("originalName", oldUser.somename());
			try {
				filledForm.data().put("roles",
						Formatters.print(User.class.getField("roles"), oldUser.roles));
			} catch (Exception e) {
				throw new Error();
			}
			filledForm.data().put("lastLogin", Formatters.print(oldUser.lastLogin));
			return badRequest(views.html.user.editUser.render(filledForm));
		}
		User user = filledForm.get();
		boolean emailChanged = !StringUtils.equalsIgnoreCase(oldUser.email, user.email);
		if (emailChanged) {
			user.emailValidated = false;
		} else {
			user.emailValidated = oldUser.emailValidated;
		}
		user.update(id);
		User self = ContextAugmenterAction.getLoggedUser();
		if (self.equals(user) && emailChanged) {
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("label.your.email.has.been.changed.you.need.relogon"));
			return redirect(routes.HomePageController.index());
		}
		return redirect(routes.UserController.list());
	}

	@Transactional
	@Restrict(@Group(RoleName.ADMIN))
	public static Result remove(long id) {
		User user = User.find.byId(id);
		if (user == null) {
			return Application.notFoundObject(User.class, id);
		}
		if (user.equals(ContextAugmenterAction.getLoggedUser())) {
			return badRequest("cannot delete yourself");
		}
		user.deleteGracefully();
		return ok();
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result changePassword(long id) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		User user = User.find.byId(id);
		if (user == null) {
			return Application.notFoundObject(User.class, id);
		}
		return ok(views.html.account.password_change.render(Account.PASSWORD_CHANGE_FORM, user));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result doChangePassword(long id) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		User user = User.find.byId(id);
		if (user == null) {
			return Application.notFoundObject(User.class, id);
		}
		final Form<Account.PasswordChange> filledForm = Account.PASSWORD_CHANGE_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(views.html.account.password_change.render(filledForm, user));
		}
		final String newPassword = filledForm.get().password;
		user.changePassword(new MyUsernamePasswordAuthUser(newPassword), true);
		flash(Application.FLASH_MESSAGE_KEY,
				Messages.get("playauthenticate.change_password.success"));
		return redirect(routes.UserController.list());
	}

	@SubjectPresent
	public static Result details(long id) {
		// get general information about user
		return TODO;
	}

}
