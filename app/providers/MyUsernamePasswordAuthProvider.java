package providers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import models.user.LinkedAccount;
import models.user.TokenAction;
import models.user.TokenAction.Type;
import models.user.User;
import play.Application;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import akka.actor.Cancellable;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.providers.password.SessionUsernamePasswordAuthUser;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.NameIdentity;

import controllers.routes;

public class MyUsernamePasswordAuthProvider extends AuthProvider {

	private static final String PROVIDER_KEY = "password";
	private static final String SETTING_KEY_MAIL = "mail";
	private static final String SETTING_KEY_MAIL_FROM_EMAIL = Mailer.SettingKeys.FROM_EMAIL;
	private static final String SETTING_KEY_MAIL_DELAY = Mailer.SettingKeys.DELAY;
	private static final String SETTING_KEY_MAIL_FROM = Mailer.SettingKeys.FROM;
	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL + "."
			+ "verificationLink.secure";
	private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL + "."
			+ "passwordResetLink.secure";
	private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";
	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

	public static final Form<MySignup> SIGNUP_FORM = Form.form(MySignup.class);
	public static final Form<MyLogin> LOGIN_FORM = Form.form(MyLogin.class);

	protected Mailer mailer;

	@Inject
	public MyUsernamePasswordAuthProvider(Application app) {
		super(app);
	}

	@Override
	public void onStart() {
		super.onStart();
		mailer = Mailer.getCustomMailer(getConfiguration().getConfig(SETTING_KEY_MAIL));
	}

	@Override
	protected List<String> neededSettingKeys() {
		List<String> needed = new ArrayList<>(Arrays.asList(SETTING_KEY_MAIL + "."
				+ SETTING_KEY_MAIL_DELAY, SETTING_KEY_MAIL + "." + SETTING_KEY_MAIL_FROM + "."
				+ SETTING_KEY_MAIL_FROM_EMAIL));
		needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
		needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
		return needed;
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	public Object authenticate(Context context, Object payload) throws AuthException {

		if (payload == Case.SIGNUP) {
			MySignup signup = getSignup(context);
			MyUsernamePasswordAuthUser authUser = buildSignupAuthUser(signup, context);
			MySignupResult r = signupUser(authUser);

			switch (r) {
			case USER_EXISTS:
				// The user exists already
				return userExists(authUser).url();
			case USER_EXISTS_UNVERIFIED:
			case USER_CREATED_UNVERIFIED:
				// User got created as unverified
				// Send validation email
				sendVerifyEmailMailing(context, authUser);
				return userUnverified(authUser).url();
			case USER_CREATED:
				// continue to login...
				return transformAuthUser(authUser, context);
			default:
				throw new AuthException("Something in signup went wrong");
			}
		} else if (payload == Case.LOGIN) {
			MyLogin login = getLogin(context);
			MyLoginUsernamePasswordAuthUser authUser = buildLoginAuthUser(login, context);
			MyLoginResult r = loginUser(authUser);
			switch (r) {
			case USER_UNVERIFIED:
				// The email of the user is not verified, yet - we won't allow
				// him to log in
				return userUnverified(authUser).url();
			case USER_LOGGED_IN:
				// The user exists and the given password was correct
				return authUser;
			case WRONG_PASSWORD:
				// don't expose this - it might harm users privacy if anyone
				// knows they signed up for our service
			case NOT_FOUND:
				// forward to login page
				return onLoginUserNotFound(context);
			default:
				throw new AuthException("Something in login went wrong");
			}
		} else {
			return PlayAuthenticate.getResolver().login().url();
		}
	}

	private Object onLoginUserNotFound(Context context) {
		context.flash().put(controllers.Application.FLASH_ERROR_KEY,
				Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
		return PlayAuthenticate.getResolver().login().url();
	}

	public static Result handleLogin(Context ctx) {
		return PlayAuthenticate.handleAuthentication(PROVIDER_KEY, ctx, Case.LOGIN);
	}

	@Override
	public AuthUser getSessionAuthUser(String id, long expires) {
		return new SessionUsernamePasswordAuthUser(getKey(), id, expires);
	}

	public static Result handleSignup(Context ctx) {
		return PlayAuthenticate.handleAuthentication(PROVIDER_KEY, ctx, Case.SIGNUP);
	}

	private MySignup getSignup(Context ctx) {
		Http.Context.current.set(ctx);
		Form<MySignup> filledForm = getSignupForm().bindFromRequest();
		return filledForm.get();
	}

	private MyLogin getLogin(Context ctx) {
		Http.Context.current.set(ctx);
		Form<MyLogin> filledForm = getLoginForm().bindFromRequest();
		return filledForm.get();
	}

	private String getEmailName(MyUsernamePasswordAuthUser user) {
		String name = null;
		if (user instanceof NameIdentity) {
			name = ((NameIdentity) user).getName();
		}

		return getEmailName(user.getEmail(), name);
	}

	private String getEmailName(String email, String name) {
		return Mailer.getEmailName(email, name);
	}

	private String generateVerificationRecord(MyUsernamePasswordAuthUser user) {
		return generateVerificationRecord(User.findByAuthUserIdentity(user));
	}

	private void sendVerifyEmailMailing(Context ctx, MyUsernamePasswordAuthUser user) {
		String subject = getVerifyEmailMailingSubject(user, ctx);
		String record = generateVerificationRecord(user);
		Body body = getVerifyEmailMailingBody(record, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	/**
	 * Called to send mails.
	 * 
	 * @param subject
	 *            The mail's subject.
	 * @param body
	 *            The mail's body.
	 * @param recipient
	 *            The (formatted) recipient.
	 * @return The {@link akka.actor.Cancellable} that can be used to cancel the
	 *         action.
	 */
	private Cancellable sendMail(String subject, Body body, String recipient) {
		return sendMail(new Mail(subject, body, new String[] { recipient }));
	}

	/**
	 * Send a pre-assembled mail.
	 * 
	 * @param mail
	 *            The mail to be sent.
	 * @return The {@link akka.actor.Cancellable} that can be used to cancel the
	 *         action.
	 */
	private Cancellable sendMail(Mail mail) {
		return mailer.sendMail(mail);
	}

	@Override
	public boolean isExternal() {
		return false;
	}

	private String getVerifyEmailMailingSubject(MyUsernamePasswordAuthUser user, Context ctx) {
		return Messages.get("playauthenticate.password.verify_signup.subject");
	}

	private Body getVerifyEmailMailingBody(String token, MyUsernamePasswordAuthUser user,
			Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_VERIFICATION_LINK_SECURE);
		String url = routes.Signup.verify(token).absoluteURL(ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		String langCode = lang.code();

		String html = getEmailTemplate("views.html.account.signup.email.verify_email", langCode,
				url, token, user.getName(), user.getEmail());
		String text = getEmailTemplate("views.txt.account.signup.email.verify_email", langCode,
				url, token, user.getName(), user.getEmail());

		return new Body(text, html);
	}

	private MyLoginUsernamePasswordAuthUser buildLoginAuthUser(MyLogin login, Context ctx) {
		return new MyLoginUsernamePasswordAuthUser(login.getPassword(), login.getEmail());
	}

	/**
	 * This gets called when the user shall be logged in directly after signing
	 * up
	 * 
	 * @param authUser
	 * @param context
	 * @return
	 */
	private MyLoginUsernamePasswordAuthUser transformAuthUser(MyUsernamePasswordAuthUser authUser,
			Context context) {
		return new MyLoginUsernamePasswordAuthUser(authUser.getEmail());
	}

	private MyUsernamePasswordAuthUser buildSignupAuthUser(MySignup signup, Context ctx) {
		return new MyUsernamePasswordAuthUser(signup);
	}

	private MyLoginResult loginUser(MyLoginUsernamePasswordAuthUser authUser) {
		User u = User.findByUsernamePasswordIdentity(authUser);
		if (u == null) {
			return MyLoginResult.NOT_FOUND;
		} else {
			if (!u.emailValidated) {
				return MyLoginResult.USER_UNVERIFIED;
			} else {
				for (LinkedAccount acc : u.linkedAccounts) {
					if (getKey().equals(acc.providerKey)) {
						if (authUser.checkPassword(acc.providerUserId, authUser.getPassword())) {
							// Password was correct
							return MyLoginResult.USER_LOGGED_IN;
						} else {
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							return MyLoginResult.WRONG_PASSWORD;
						}
					}
				}
				return MyLoginResult.WRONG_PASSWORD;
			}
		}
	}

	private MySignupResult signupUser(MyUsernamePasswordAuthUser user) {
		User u = User.findByUsernamePasswordIdentity(user);
		if (u != null) {
			if (u.emailValidated) {
				// This user exists, has its email validated and is active
				return MySignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				return MySignupResult.USER_EXISTS_UNVERIFIED;
			}
		}
		// The user either does not exist or is inactive - create a new one
		User.create(user);
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		return MySignupResult.USER_CREATED_UNVERIFIED;
	}

	private Form<MySignup> getSignupForm() {
		return SIGNUP_FORM;
	}

	private Form<MyLogin> getLoginForm() {
		return LOGIN_FORM;
	}

	private Call userExists(UsernamePasswordAuthUser authUser) {
		return routes.Signup.exists();
	}

	private Call userUnverified(UsernamePasswordAuthUser authUser) {
		return routes.Signup.unverified();
	}

	private String generateVerificationRecord(User user) {
		final String token = generateToken();
		// Do database actions, etc.
		TokenAction.create(Type.EMAIL_VERIFICATION, token, user);
		return token;
	}

	private Body getPasswordResetMailingBody(String token, User user, Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		String url = routes.Signup.resetPassword(token).absoluteURL(ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		String langCode = lang.code();

		String html = getEmailTemplate("views.html.account.email.password_reset", langCode, url,
				token, user.somename(), user.email);
		String text = getEmailTemplate("views.txt.account.email.password_reset", langCode, url,
				token, user.somename(), user.email);

		return new Body(text, html);
	}

	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

	private String generatePasswordResetRecord(User u) {
		String token = generateToken();
		TokenAction.create(Type.PASSWORD_RESET, token, u);
		return token;
	}

	private String getPasswordResetMailingSubject(User user, Context ctx) {
		return Messages.get("playauthenticate.password.reset_email.subject");
	}

	public void sendPasswordResetMailing(User user, Context ctx) {
		String token = generatePasswordResetRecord(user);
		String subject = getPasswordResetMailingSubject(user, ctx);
		Body body = getPasswordResetMailingBody(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	private String getVerifyEmailMailingSubjectAfterSignup(User user, Context ctx) {
		return Messages.get("playauthenticate.password.verify_email.subject");
	}

	private String getEmailTemplate(String template, String langCode, String url, String token,
			String name, String email) {
		Class<?> cls = null;
		String ret = null;
		try {
			cls = Class.forName(template + "_" + langCode);
		} catch (ClassNotFoundException e) {
			Logger.warn("Template: '" + template + "_" + langCode
					+ "' was not found! Trying to use English fallback template instead.");
		}
		if (cls == null) {
			try {
				cls = Class.forName(template + "_" + EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
			} catch (ClassNotFoundException e) {
				Logger.error("Fallback template: '" + template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE + "' was not found either!");
			}
		}
		if (cls != null) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod("render", String.class, String.class, String.class,
						String.class);
				ret = htmlRender.invoke(null, url, token, name, email).toString();

			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private Body getVerifyEmailMailingBodyAfterSignup(String token, User user, Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_VERIFICATION_LINK_SECURE);
		String url = routes.Signup.verify(token).absoluteURL(ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		String langCode = lang.code();

		String html = getEmailTemplate("views.html.account.email.verify_email", langCode, url,
				token, user.somename(), user.email);
		String text = getEmailTemplate("views.txt.account.email.verify_email", langCode, url,
				token, user.somename(), user.email);

		return new Body(text, html);
	}

	public void sendVerifyEmailMailingAfterSignup(User user, Context ctx) {

		String subject = getVerifyEmailMailingSubjectAfterSignup(user, ctx);
		String token = generateVerificationRecord(user);
		Body body = getVerifyEmailMailingBodyAfterSignup(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	private String getEmailName(User user) {
		return getEmailName(user.email, user.somename());
	}

	public static MyUsernamePasswordAuthProvider getProvider() {
		return (MyUsernamePasswordAuthProvider) PlayAuthenticate.getProvider(PROVIDER_KEY);
	}

	public static class MyIdentity {

		public MyIdentity() {
		}

		public MyIdentity(String email) {
			this.email = email;
		}

		@Required
		@Email
		public String email;

	}

	public static class MyLogin extends MyIdentity {

		@Required
		@MinLength(5)
		public String password;

		public String getEmail() {
			return email;
		}

		public String getPassword() {
			return password;
		}
	}

	public static class MySignup extends MyLogin {

		@Required
		@MinLength(5)
		public String repeatPassword;

		@Required
		public String name;

		public String validate() {
			if (password == null || !password.equals(repeatPassword)) {
				return Messages.get("playauthenticate.password.signup.error.passwords_not_same");
			}
			return null;
		}
	}

	private enum Case {
		SIGNUP, LOGIN
	}

	private enum MySignupResult {
		USER_EXISTS, USER_CREATED_UNVERIFIED, USER_CREATED, USER_EXISTS_UNVERIFIED
	}

	private enum MyLoginResult {
		USER_UNVERIFIED, USER_LOGGED_IN, NOT_FOUND, WRONG_PASSWORD
	}

}
