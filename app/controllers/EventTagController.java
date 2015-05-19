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

	public static Result edit(EventTag tag) {
		return ok(views.html.event.editEventTag.render(EDIT_FORM.fill(tag), tag));
	}

	public static Result doEdit(EventTag oldTag) {
		Form<EventTag> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEventTag.render(filledForm, oldTag));
		}
		EventTag tag = filledForm.get();
		tag.update(oldTag.id);
		return redirect(routes.EventTagController.list());
	}

	public static Result add() {
		return ok(views.html.event.editEventTag.render(EDIT_FORM, null));
	}

	public static Result doAdd() {
		Form<EventTag> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEventTag.render(filledForm, null));
		}
		EventTag tag = filledForm.get();
		tag.save();
		return redirect(routes.EventTagController.list());
	}

	public static Result remove(EventTag tag) {
		tag.delete();
		return ok();
	}
}
