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
				return Messages.get("playauthenticate.change_password.error.passwords_not_same");
			}
			return null;
		}
	}

	public static final Form<PasswordChange> PASSWORD_CHANGE_FORM = form(PasswordChange.class);

	public static class ProfileChange {

		@Email
		@Required
		public String email;

		@Required
		public String name;

		public ProfileChange() {
			// no op
		}

		public ProfileChange(User user) {
			this.email = user.email;
			this.name = user.name;
		}

	}

	public static final Form<ProfileChange> PROFILE_CHANGE_FORM = form(ProfileChange.class);

	@SubjectPresent
	public Result link() {
		Application.noCache(response());
		return ok(link.render());
	}

	@Restrict(@Group(RoleName.USER))
	public Result verifyEmail() {
		Application.noCache(response());
		User user = ContextAugmenterAction.getLoggedUser();
		if (user.emailValidated) {
			// E-Mail has been validated already
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.verify_email.error.already_validated"));
		} else if (StringUtils.isNotBlank(user.email)) {
			flash(Application.FLASH_MESSAGE_KEY, Messages.get(
					"playauthenticate.verify_email.message.instructions_sent", user.email));
			MyUsernamePasswordAuthProvider.getProvider().sendVerifyEmailMailingAfterSignup(user,
					ctx());
		} else {
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.verify_email.error.set_email_first", user.email));
		}
		return redirect(routes.Account.profile());
	}

	@SubjectPresent
	public Result askLink() {
		Application.noCache(response());
		AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to index
			return redirect(routes.HomePageController.index());
		}
		return ok(ask_link.render(ACCEPT_FORM, u));
	}

	@SubjectPresent
	public Result doLink() {
		Application.noCache(response());
		AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to index
			return redirect(routes.HomePageController.index());
		}

		Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(ask_link.render(filledForm, u));
		}
		// User made a choice :)
		boolean link = filledForm.get().accept;
		if (link) {
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.accounts.link.success"));
		}
		return PlayAuthenticate.link(ctx(), link);
	}

	@SubjectPresent
	public Result askMerge() {
		Application.noCache(response());
		// this is the currently logged in user
		AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to index
			return redirect(routes.HomePageController.index());
		}

		// You could also get the local user object here via
		// User.findByAuthUserIdentity(newUser)
		return ok(ask_merge.render(ACCEPT_FORM, aUser, bUser));
	}

	@SubjectPresent
	public Result doMerge() {
		Application.noCache(response());
		// this is the currently logged in user
		AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to index
			return redirect(routes.HomePageController.index());
		}

		Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to merge or not merge
			return badRequest(ask_merge.render(filledForm, aUser, bUser));
		}
		// User made a choice :)
		boolean merge = filledForm.get().accept;
		if (merge) {
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.accounts.merge.success"));
		}
		return PlayAuthenticate.merge(ctx(), merge);
	}

	@SubjectPresent
	public Result profile() {
		User user = ContextAugmenterAction.getLoggedUser();
		Form<ProfileChange> filledForm = PROFILE_CHANGE_FORM.fill(new ProfileChange(user));
		return ok(views.html.account.profile.render(filledForm));
	}

	@SubjectPresent
	public Result doChangeProfile() {
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
		if (!pc.name.equals(user.name)) {
			user.name = pc.name;
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

	public Result changePassword(User user) {
		Application.noCache(response());
		if (user == null) {
			return badRequest();
		}
		return ok(views.html.account.password_change.render(PASSWORD_CHANGE_FORM, null));
	}

	public Result changePersonalPassword() {
		User u = ContextAugmenterAction.getLoggedUser();

		if (!u.emailValidated) {
			return ok(unverified.render());
		}
		return changePassword(u);
	}

	public Result doChangePersonalPassword() {
		Application.noCache(response());
		User user = ContextAugmenterAction.getLoggedUser();
		Form<PasswordChange> filledForm = PASSWORD_CHANGE_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(views.html.account.password_change.render(filledForm, null));
		}
		String newPassword = filledForm.get().password;
		user.changePassword(newPassword, true);
		flash(Application.FLASH_MESSAGE_KEY,
				Messages.get("playauthenticate.change_password.success"));
		return redirect(routes.Account.profile());
	}

}