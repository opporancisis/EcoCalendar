package controllers;

import static java.util.stream.Collectors.toList;
import static play.data.Form.form;

import java.util.ArrayList;
import java.util.List;

import models.sys.Setting;
import models.sys.SettingName;
import models.user.RoleName;
import models.user.SecurityRole;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import play.data.Form;
import play.data.format.Formatters;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonResponse;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.MoreObjects;

import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class UserController extends Controller {

	private static final Form<UserProperties> EDIT_FORM = form(UserProperties.class);
	private static final Form<PasswordChange> PASSWORD_CHANGE_FORM = form(PasswordChange.class);
	private static final Form<PasswordChangePresentation> PASSWORD_CHANGE_PRESENTATION_FORM = form(PasswordChangePresentation.class);

	@Restrict(@Group(RoleName.ADMIN))
	public Result manage() {
		Application.noCache(response());
		return ok(views.html.user.manageUsers.render(EDIT_FORM, PASSWORD_CHANGE_PRESENTATION_FORM));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result getUsers() {
		List<User> users = User.find.query().where()
				.ne("id", Setting.getLong(SettingName.DELETED_USER_ID)).findList();
		ArrayNode arr = Json.newArray();
		users.forEach(u -> arr.add(Json.toJson(new UserProperties(u))));
		return ok(arr);
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result getUser(User user) {
		return ok(Json.toJson(new UserProperties(user)));
	}

	@Restrict(@Group(RoleName.ADMIN))
	@BodyParser.Of(play.mvc.BodyParser.Json.class)
	public Result updateUser(User user) {
		JsonNode json = request().body().asJson();
		Form<UserProperties> filledForm = EDIT_FORM.bind(json);
		if (filledForm.hasErrors()) {
			return badRequest(filledForm.errorsAsJson());
		}
		UserProperties userProps = filledForm.get();
		User self = ContextAugmenterAction.getLoggedUser();
		if (self.equals(user) && !userProps.hasRole(RoleName.ADMIN)) {
			return badRequest(Json.newObject().set("roles",
					Json.newArray().add("error.cannot.revoke.ADMIN.role.from.yourself"))
			/* JsonResponse.buildError("cannot revoke ADMIN role") */);
		}
		userProps.updateUser(user);
		return ok(Json.toJson(userProps));
	}

	@Restrict(@Group(RoleName.ADMIN))
	@BodyParser.Of(play.mvc.BodyParser.Json.class)
	public Result createUser() {
		JsonNode json = request().body().asJson();
		Form<UserProperties> filledForm = EDIT_FORM.bind(json);
		if (filledForm.hasErrors()) {
			return badRequest(filledForm.errorsAsJson());
		}
		UserProperties userProps = filledForm.get();
		return ok(Json.toJson(new UserProperties(userProps.createUser())));
	}

	@Restrict(@Group(RoleName.ADMIN))
	@BodyParser.Of(play.mvc.BodyParser.Json.class)
	public Result changeUserPassword(User user) {
		JsonNode json = request().body().asJson();
		Form<PasswordChange> filledForm = PASSWORD_CHANGE_FORM.bind(json);
		if (filledForm.hasErrors()) {
			return badRequest(filledForm.errorsAsJson());
		}
		String newPassword = filledForm.get().password;
		user.changePassword(newPassword, true);
		return ok();
	}

	@Transactional
	@Restrict(@Group(RoleName.ADMIN))
	public Result removeUser(User user) {
		if (user.equals(ContextAugmenterAction.getLoggedUser())) {
			return badRequest(JsonResponse.buildError(Messages.get("error.cannot.remove.yourself")));
		}
		user.deleteGracefully();
		return ok();
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

		@Required
		public List<String> roles;

		@Required
		public String name;

		@Pattern(value = "\\d{5,10}", message = "error.phone")
		public String phone;

		@URL
		public String profileLink;

		public UserProperties() {
			// no-op
		}

		public UserProperties(User user) {
			this.id = user.id;
			this.blocked = user.blocked;
			this.emailValidated = user.emailValidated;
			this.email = user.email;
			this.roles = user.roles.stream().map(r -> r.roleName).collect(toList());
			this.name = user.name;
			this.phone = user.phone;
			this.profileLink = user.profileLink;
		}

		private void setFields(User user) {
			user.blocked = MoreObjects.firstNonNull(this.blocked, Boolean.FALSE);
			user.emailValidated = this.emailValidated;
			user.email = this.email;
			user.roles = this.roles.stream().map(SecurityRole::findByRoleName).collect(toList());
			user.name = this.name;
			user.phone = this.phone;
			user.profileLink = this.profileLink;
		}

		public void updateUser(User user) {
			setFields(user);
			user.update();
		}

		public User createUser() {
			User user = new User();
			setFields(user);
			user.save();
			return user;
		}

		public boolean hasRole(String role) {
			return roles.contains(role);
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

	public static class PasswordChange {
		@MinLength(5)
		@Required
		public String password;
	}

	/**
	 * Used only to draw form in html. For fetching parameters is used
	 * PasswordChange.
	 *
	 */
	public static class PasswordChangePresentation {
		@MinLength(5)
		@Required
		public String password;

		@MinLength(5)
		@Required
		public String repeatPassword;

	}

}
