package controllers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import javax.inject.Inject;

import jsmessages.JsMessages;
import jsmessages.JsMessagesFactory;
import jsmessages.japi.Helper;
import play.Routes;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Scala;
import play.mvc.Controller;
import play.mvc.Http.Response;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";

	public static final Lang DEF_LANG = Lang.forCode("ru");

	private JsMessages MESSAGES;

	@Inject
	public Application(JsMessagesFactory jsMessagesFactory) {
		MESSAGES = jsMessagesFactory.all();
	}
	
	public Result login() {
		return ok(views.html.login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
	}

	public Result doLogin() {
		noCache(response());
		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(views.html.login.render(filledForm));
		} else {
			// Everything was filled
			return MyUsernamePasswordAuthProvider.handleLogin(ctx());
		}
	}

	public Result signup() {
		return ok(views.html.signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
	}

	public Result jsRoutes() {
		return ok(
				Routes.javascriptRouter("jsRoutes", routes.javascript.Signup.forgotPassword(),
						routes.javascript.EventController.edit(),
						routes.javascript.EventController.removeMany(),
						routes.javascript.UserController.details(),
						routes.javascript.GrandEventController.details(),
						routes.javascript.OrganizationController.details(),
						routes.javascript.FileController.doAdd(),
						routes.javascript.MessageController.removeMany(),
						routes.javascript.MessageController.markAsReadMany()))
				.as("text/javascript");
	}

	public Result doSignup() {
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
			return MyUsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}

	public static String formatTimestamp(long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

	public static Result notFoundObject(Class<?> clazz, Object id) {
		return notFoundObject(clazz.getSimpleName(), id);
	}

	public static Result notFoundObject(String clazz, Object id) {
		return notFound(views.html.notFoundObject.render(clazz, id));
	}

	public static Result forbiddenPage() {
		return forbidden(views.html.forbidden.render());
	}

	public Result jsMessages() {
		return ok(MESSAGES.apply(Scala.Option("window.Messages"), Helper.messagesFromCurrentHttpContext()));
	}

	public static LocalDateTime now() {
		return LocalDateTime.now();
	}

	public static void noCache(Response response) {
		com.feth.play.module.pa.controllers.AuthenticateBase.noCache(response);
	}

}