package controllers;

import models.event.tag.EventTag;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class EventTagController extends Controller {

	private static final Form<EventTagProps> EDIT_FORM = Form.form(EventTagProps.class);

	public Result list() {
		return ok(views.html.event.listEventTags.render(EventTag.find.all()));
	}

	public Result edit(EventTag tag) {
		return ok(views.html.event.editEventTag.render(EDIT_FORM.fill(new EventTagProps(tag)), tag));
	}

	public Result doEdit(EventTag et) {
		Form<EventTagProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEventTag.render(filledForm, et));
		}
		EventTagProps etProps = filledForm.get();
		etProps.updateEventTag(et);
		return redirect(routes.EventTagController.list());
	}

	public Result add() {
		return ok(views.html.event.editEventTag.render(EDIT_FORM, null));
	}

	public Result doAdd() {
		Form<EventTagProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEventTag.render(filledForm, null));
		}
		EventTagProps etProps = filledForm.get();
		etProps.createEventTag();
		return redirect(routes.EventTagController.list());
	}

	public Result remove(EventTag tag) {
		tag.delete();
		return ok();
	}

	public static class EventTagProps {

		@Required
		public String name;

		public EventTagProps() {
			// no-op
		}

		public EventTagProps(EventTag et) {
			this.name = et.name;
		}

		private void setFields(EventTag et) {
			et.name = this.name;
		}

		public void updateEventTag(EventTag et) {
			setFields(et);
			et.update();
		}

		public EventTag createEventTag() {
			EventTag et = new EventTag();
			setFields(et);
			et.save();
			return et;
		}
	}
}
