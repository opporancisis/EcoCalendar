package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import jsmessages.JsMessages;
import play.Play;
import play.Routes;
import play.data.Form;
import play.i18n.Lang;
import play.mvc.Controller;
import play.mvc.Http.Response;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;

import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";

	public static final Lang DEF_LANG = Lang.forCode("ru");

	private static final JsMessages MESSAGES = JsMessages.create(Play.application());

	public static Result justOk() {
		// is used for keep alive requests from Rasberry
		return ok();
	}

	public static Result login() {
		return ok(views.html.login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
	}

	public static Result doLogin() {
		noCache(response());
		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(views.html.login.render(filledForm));
		} else {
			// Everything was filled
			return UsernamePasswordAuthProvider.handleLogin(ctx());
		}
	}

	public static Result signup() {
		return ok(views.html.signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
	}

	public static Result jsRoutes() {
		return ok(
				Routes.javascriptRouter("jsRoutes", routes.javascript.Signup.forgotPassword(),
						routes.javascript.EventController.edit(),
						routes.javascript.EventController.remove(),
						routes.javascript.UserController.details(),
						routes.javascript.GrandEventController.details(),
						routes.javascript.OrganizationController.details(),
						routes.javascript.FileController.doCreate(),
						routes.javascript.MessageController.removeMany(),
						routes.javascript.MessageController.markAsReadMany()))
				.as("text/javascript");
	}

	public static Result doSignup() {
		noCache(response());
		final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(views.html.signup.render(filledForm));
		} else {
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return UsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}

	public static String formatTimestamp(long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

	public static Result notFoundObject(Class<?> clazz, Object id) {
		return notFound(views.html.notFoundObject.render(clazz, id));
	}

	public static Result forbiddenPage() {
		return forbidden(views.html.forbidden.render());
	}

	public static Result jsMessages() {
		return ok(MESSAGES.generate("window.Messages"));
	}

	public static void noCache(Response response) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response);
	}

}