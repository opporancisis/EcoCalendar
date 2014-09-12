package controllers;

import static security.MyDynamicResourceHandler.CHECK_AUTHORSHIP;

import java.util.List;

import models.message.Message;
import models.user.User;
import play.Logger;
import play.Logger.ALogger;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import security.MyDynamicResourceHandler;
import be.objectify.deadbolt.java.actions.Dynamic;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class MessageController extends Controller {

	private static final ALogger log = Logger.of(MessageController.class);

	private static final String CHECK_AUTHORSHIP_TOPIC = CHECK_AUTHORSHIP
			+ Message.CLAZZ;

	public static Result list() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		List<Message> messages = Message.find.query().where()
				.eq("owner", ContextAugmenterAction.getLoggedUser()).findList();
		return ok(views.html.message.listMessages.render(messages));
	}

	@Dynamic(CHECK_AUTHORSHIP_TOPIC)
	public static Result read(long id) {
		Message message = MyDynamicResourceHandler
				.getAuthoredObject(Message.class);
		if (message == null) {
			return Application.notFoundObject(Message.class, id);
		}
		// TODO: mark as read a bit latter after message has been opened - in a
		// 5 seconds for example. Via ajax or WebSockets
		message.unread = false;
		message.update();
		return ok(views.html.message.viewMessage.render(message));
	}

	@Dynamic(CHECK_AUTHORSHIP_TOPIC)
	public static Result remove(long id) {
		Message message = MyDynamicResourceHandler
				.getAuthoredObject(Message.class);
		if (message == null) {
			return Application.notFoundObject(Message.class, id);
		}
		message.delete();
		return ok();
	}

	@Transactional
	public static Result removeMany() {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		}
		if (!json.isArray()) {
			return badRequest("root element must be array: " + json);
		}
		log.debug("json received: {}", json);
		User user = ContextAugmenterAction.getLoggedUser();
		for (JsonNode elem : json) {
			long id = elem.asLong(-1);
			if (id == -1) {
				return badRequest("incorrect format. expected long, but get: "
						+ elem);
			}
			Message message = Message.find.byId(id);
			if (!user.equals(message.owner)) {
				return badRequest("hacker? no way! you are trying to delete not your messages!");
			}
			message.delete();
		}
		return ok();
	}
}
