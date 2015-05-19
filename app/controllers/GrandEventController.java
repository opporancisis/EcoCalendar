package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import models.event.GrandEvent;
import models.event.tag.EventTag;
import models.organization.Organization;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class GrandEventController extends Controller {

	private static final Form<GrandEvent> EDIT_FORM = Form.form(GrandEvent.class);

	public static Result list() {
		return ok(views.html.grandevent.listGrandEvents.render(GrandEvent.find.query()
				.order("startDate").findList()));
	}

	public static Result edit(GrandEvent event) {
		return ok(views.html.grandevent.editGrandEvent.render(EDIT_FORM.fill(event), event,
				Organization.find.all(), EventTag.find.all()));
	}

	public static Result doEdit(GrandEvent oldEvent) {
		Form<GrandEvent> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.grandevent.editGrandEvent.render(filledForm, oldEvent,
					Organization.find.all(), EventTag.find.all()));
		}
		GrandEvent event = filledForm.get();
		event.update(oldEvent.id);
		// TODO: if (1) start/end date changed and (2) this GE has some events
		// and (3) these events are out of start-end period, then:
		// 1. exclude out-of-period events from GE
		// 2. tell GE-editor about changes via flash message
		// 3. notify creators of excluded events via email (if creator !=
		// current GE-editor)
		return redirect(routes.GrandEventController.list());
	}

	public static Result add() {
		return ok(views.html.grandevent.editGrandEvent.render(EDIT_FORM, null,
				Organization.find.all(), EventTag.find.all()));
	}

	@SubjectPresent
	public static Result doAdd() {
		Form<GrandEvent> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.grandevent.editGrandEvent.render(filledForm, null,
					Organization.find.all(), EventTag.find.all()));
		}
		GrandEvent event = filledForm.get();
		event.author = ContextAugmenterAction.getLoggedUser();
		// TODO:
		// 1. count karma amount of creator
		// 2. according to it set published field
		// 3. if not published, then send admins/moderators invitation to review
		// event
		event.save();
		return redirect(routes.GrandEventController.list());
	}

	// TODO: who is able to delete grand events? restrict access
	public static Result remove(GrandEvent event) {
		event.delete();
		return ok();
	}

	public static Result details(GrandEvent event) {
		return TODO;
	}
}
