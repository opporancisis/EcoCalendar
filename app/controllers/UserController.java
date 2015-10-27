package controllers;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.sys.Setting;
import models.sys.SettingName;
import models.user.RoleName;
import models.user.SecurityRole;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;

import play.data.Form;
import play.data.format.Formatters;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Pattern;
import play.data.validation.ValidationError;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthUser;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.ExpressionList;
import com.google.common.base.MoreObjects;

import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class UserController extends Controller {

	private static final Form<UserProperties> EDIT_FORM = form(UserProperties.class);

	@Restrict(@Group(RoleName.ADMIN))
	public Result list() {
		Application.noCache(response());
		return ok(views.html.user.listUsers.render(User.find.query().where()
				.ne("id", Setting.getLong(SettingName.DELETED_USER_ID)).findList()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result add() {
		return ok(views.html.user.editUser.render(EDIT_FORM, null));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result doAdd() {
		Form<UserProperties> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.user.editUser.render(filledForm, null));
		}
		UserProperties userProps = filledForm.get();
		User user = userProps.createUser();
		user.save();
		return redirect(routes.UserController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result edit(User user) {
		Form<UserProperties> filledForm = EDIT_FORM.fill(new UserProperties(user));
		return ok(views.html.user.editUser.render(filledForm, user));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result doEdit(User user) {
		Form<UserProperties> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			try {
				filledForm.data().put("roles",
						Formatters.print(User.class.getField("roles"), user.roles));
			} catch (Exception e) {
				throw new Error();
			}
			return badRequest(views.html.user.editUser.render(filledForm, user));
		}
		UserProperties userProps = filledForm.get();
		if (!userProps.hasRole(RoleName.ADMIN) && user.hasRole(RoleName.ADMIN)) {
			return badRequest("cannot revoke ADMIN role");
		}
		boolean emailChanged = !StringUtils.equalsIgnoreCase(user.email, userProps.email);
		userProps.updateUser(user);
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
	public Result remove(User user) {
		if (user.equals(ContextAugmenterAction.getLoggedUser())) {
			return badRequest("cannot delete yourself");
		}
		user.deleteGracefully();
		return ok();
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result changePassword(User user) {
		Application.noCache(response());
		return ok(views.html.account.password_change.render(Account.PASSWORD_CHANGE_FORM, user));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result doChangePassword(User user) {
		Application.noCache(response());
		Form<Account.PasswordChange> filledForm = Account.PASSWORD_CHANGE_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(views.html.account.password_change.render(filledForm, user));
		}
		String newPassword = filledForm.get().password;
		user.changePassword(newPassword, true);
		flash(Application.FLASH_MESSAGE_KEY,
				Messages.get("playauthenticate.change_password.success"));
		return redirect(routes.UserController.list());
	}

	@SubjectPresent
	public Result details(User user) {
		// get general information about user
		return TODO;
	}

	public static class UserProperties {

		public Long id;

		public Boolean blocked;

		public boolean emailValidated;

		@Email
		public String email;

		public List<SecurityRole> roles;

		public String name;

		public String phone;

		@URL
		public String profileLink;

		public String note;

		public UserProperties() {
			// no-op
		}

		public UserProperties(User user) {
			this.blocked = user.blocked;
			this.emailValidated = user.emailValidated;
			this.email = user.email;
			this.roles = user.roles;
			this.name = user.name;
			this.phone = user.phone;
			this.profileLink = user.profileLink;
			this.note = user.note;
		}

		private void setFields(User user) {
			user.blocked = MoreObjects.firstNonNull(this.blocked, Boolean.FALSE);
			user.emailValidated = this.emailValidated;
			user.email = this.email;
			user.roles = this.roles;
			user.name = this.name;
			user.phone = this.phone;
			user.profileLink = this.profileLink;
			user.note = this.note;
		}

		public void updateUser(User user) {
			setFields(user);
			user.update();
		}

		public User createUser() {
			User user = new User();
			setFields(user);
			return user;
		}

		public boolean hasRole(String... aRoles) {
			Set<String> aRolesSet = new HashSet<>(Arrays.asList(aRoles));
			return hasRole(aRolesSet);
		}

		public boolean hasRole(Set<String> aRoles) {
			for (SecurityRole role : roles) {
				if (aRoles.contains(role.getName())) {
					return true;
				}
			}
			return false;
		}

		public List<ValidationError> validate() {
			List<ValidationError> errors = new ArrayList<>();
			if (StringUtils.isNotBlank(email)) {
				ExpressionList<User> exprEmail = User.find.query().where().eq("email", email);
				if ((id == null && exprEmail.findRowCount() > 0)
						|| (id != null && exprEmail.ne("id", id).findRowCount() > 0)) {
					errors.add(new ValidationError("email", Messages
							.get("error.email.must.be.unique")));
				}
			} else {
				email = null;
			}
			// phone = PhoneValidator.purify(phone);
			// if (!Strings.isNullOrEmpty(phone)) {
			// if (!PhoneValidator.isValid(phone)) {
			// errors.add(new ValidationError("phone",
			// Messages.get("error.incorrect.phone")));
			// }
			// ExpressionList<User> exprPhone =
			// User.find.query().where().eq("phone", phone);
			// if ((id == null && exprPhone.findRowCount() > 0)
			// || (id != null && exprPhone.ne("id", id).findRowCount() > 0)) {
			// errors.add(new ValidationError("phone", Messages
			// .get("error.phone.must.be.unique")));
			// }
			// }
			return errors.isEmpty() ? null : errors;
		}

	}

}
