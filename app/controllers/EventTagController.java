package controllers;

import controllers.helpers.ContextAugmenter;
import models.event.tag.EventTag;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

@ContextAugmenter
public class EventTagController extends Controller {

	private static final Form<EventTag> EDIT_FORM = Form.form(EventTag.class);

	public static Result list() {
		return ok(views.html.event.listEventTags.render(EventTag.find.all()));
	}

	public static Result edit(long id) {
		EventTag tag = EventTag.find.byId(id);
		if (tag == null) {
			return Application.notFoundObject(EventTag.class, id);
		}
		return ok(views.html.event.editEventTag.render(EDIT_FORM.fill(tag), tag));
	}

	public static Result doEdit(long id) {
		EventTag tag = EventTag.find.byId(id);
		if (tag == null) {
			return Application.notFoundObject(EventTag.class, id);
		}
		Form<EventTag> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEventTag.render(filledForm, tag));
		}
		EventTag updatedTag = filledForm.get();
		updatedTag.update(id);
		return redirect(routes.EventTagController.list());
	}

	public static Result create() {
		return ok(views.html.event.editEventTag.render(EDIT_FORM, null));
	}

	public static Result doCreate() {
		Form<EventTag> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEventTag.render(filledForm, null));
		}
		EventTag tag = filledForm.get();
		tag.save();
		return redirect(routes.EventTagController.list());
	}

	public static Result remove(long id) {
		EventTag tag = EventTag.find.byId(id);
		if (tag == null) {
			return Application.notFoundObject(EventTag.class, id);
		}
		tag.delete();
		return ok();
	}
}
