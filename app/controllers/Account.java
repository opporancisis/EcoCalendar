package controllers;

import org.apache.commons.lang3.StringUtils;

import models.user.RoleName;
import models.user.User;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;
import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthUser;
import views.html.account.*;
import static play.data.Form.form;

@ContextAugmenter
public class Account extends Controller {

	public static class Accept {

		@Required
		@NonEmpty
		public Boolean accept;

	}

	private static final Form<Accept> ACCEPT_FORM = form(Accept.class);

	public static class PasswordChange {
		@MinLength(5)
		@Required
		public String password;

		@MinLength(5)
		@Required
		public String repeatPassword;

		public String validate() {
			if (password == null || !password.equals(repeatPassword)) {
				return Messages
						.get("playauthenticate.change_password.error.passwords_not_same");
			}
			return null;
		}
	}

	public static final Form<PasswordChange> PASSWORD_CHANGE_FORM = form(PasswordChange.class);

	public static class ProfileChange {

		public ProfileChange() {
			// no op
		}

		public ProfileChange(String email, String nick) {
			this.email = email;
			this.nick = nick;
		}

		@Email
		@Required
		public String email;

		@Pattern(User.NICK_PAT)
		@Required
		public String nick;
	}

	public static final Form<ProfileChange> PROFILE_CHANGE_FORM = form(ProfileChange.class);

	@SubjectPresent
	public static Result link() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(link.render());
	}

	@Restrict(@Group(RoleName.USER))
	public static Result verifyEmail() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final User user = ContextAugmenterAction.getLoggedUser();
		if (user.emailValidated) {
			// E-Mail has been validated already
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.verify_email.error.already_validated"));
		} else if (StringUtils.isNotBlank(user.email)) {
			flash(Application.FLASH_MESSAGE_KEY, Messages.get(
					"playauthenticate.verify_email.message.instructions_sent",
					user.email));
			MyUsernamePasswordAuthProvider.getProvider()
					.sendVerifyEmailMailingAfterSignup(user, ctx());
		} else {
			flash(Application.FLASH_MESSAGE_KEY, Messages.get(
					"playauthenticate.verify_email.error.set_email_first",
					user.email));
		}
		return redirect(routes.Account.profile());
	}

	// @Restrict(@Group(Role.USER))
	// public static Result doChangePassword() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// final Form<Account.PasswordChange> filledForm = PASSWORD_CHANGE_FORM
	// .bindFromRequest();
	// if (filledForm.hasErrors()) {
	// // User did not select whether to link or not link
	// return badRequest(views.user.password_change.render(filledForm));
	// } else {
	// final User user = Application.getLocalUser(session());
	// final String newPassword = filledForm.get().password;
	// user.changePassword(new MyUsernamePasswordAuthUser(newPassword),
	// true);
	// flash(Application.FLASH_MESSAGE_KEY,
	// Messages.get("playauthenticate.change_password.success"));
	// return redirect(routes.Account.profile());
	// }
	// }

	@SubjectPresent
	public static Result askLink() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.HomePageController.index());
		}
		return ok(ask_link.render(ACCEPT_FORM, u));
	}

	@SubjectPresent
	public static Result doLink() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.HomePageController.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(ask_link.render(filledForm, u));
		} else {
			// User made a choice :)
			final boolean link = filledForm.get().accept;
			if (link) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.accounts.link.success"));
			}
			return PlayAuthenticate.link(ctx(), link);
		}
	}

	@SubjectPresent
	public static Result askMerge() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.HomePageController.index());
		}

		// You could also get the local user object here via
		// User.findByAuthUserIdentity(newUser)
		return ok(ask_merge.render(ACCEPT_FORM, aUser, bUser));
	}

	@SubjectPresent
	public static Result doMerge() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.HomePageController.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to merge or not merge
			return badRequest(ask_merge.render(filledForm, aUser, bUser));
		} else {
			// User made a choice :)
			final boolean merge = filledForm.get().accept;
			if (merge) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.accounts.merge.success"));
			}
			return PlayAuthenticate.merge(ctx(), merge);
		}
	}

	@SubjectPresent
	public static Result profile() {
		User user = ContextAugmenterAction.getLoggedUser();
		Form<ProfileChange> filledForm = PROFILE_CHANGE_FORM
				.fill(new ProfileChange(user.email, user.nick));
		return ok(views.html.account.profile.render(filledForm));
	}

	@SubjectPresent
	public static Result doChangeProfile() {
		User user = ContextAugmenterAction.getLoggedUser();
		Form<ProfileChange> filledForm = PROFILE_CHANGE_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.account.profile.render(filledForm));
		}
		ProfileChange pc = filledForm.get();
		boolean needUpdate = false;
		if (!pc.email.equals(user.email)) {
			user.email = pc.email;
			user.emailValidated = false;
			needUpdate = true;
		}
		if (!pc.nick.equals(user.nick)) {
			user.nick = pc.nick;
			needUpdate = true;
		}
		if (needUpdate) {
			user.update();
		}
		if (ContextAugmenterAction.getLoggedUser() == null) {
			return redirect(routes.HomePageController.index());
		}
		return ok(views.html.account.profile.render(filledForm));
	}

	public static Result changePassword(User user) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		if (user == null) {
			return badRequest();
		}
		return ok(views.html.account.password_change.render(
				PASSWORD_CHANGE_FORM, null));
	}

	public static Result changePersonalPassword() {
		final User u = ContextAugmenterAction.getLoggedUser();

		if (!u.emailValidated) {
			return ok(unverified.render());
		}
		return changePassword(u);
	}

	public static Result doChangePersonalPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final User user = ContextAugmenterAction.getLoggedUser();
		final Form<PasswordChange> filledForm = PASSWORD_CHANGE_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(views.html.account.password_change.render(
					filledForm, null));
		}
		final String newPassword = filledForm.get().password;
		user.changePassword(new MyUsernamePasswordAuthUser(newPassword), true);
		flash(Application.FLASH_MESSAGE_KEY,
				Messages.get("playauthenticate.change_password.success"));
		return redirect(routes.Account.profile());
	}

}