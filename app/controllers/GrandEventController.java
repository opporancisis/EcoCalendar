package controllers;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Lob;

import com.fasterxml.jackson.annotation.JsonIgnore;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import models.event.GrandEvent;
import models.event.tag.EventTag;
import models.organization.Organization;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class GrandEventController extends Controller {

	private static final Form<GrandEventProps> EDIT_FORM = Form.form(GrandEventProps.class);

	public Result list() {
		return ok(views.html.grandevent.listGrandEvents.render(GrandEvent.find.query()
				.order("startDate").findList()));
	}

	public Result edit(GrandEvent event) {
		return ok(views.html.grandevent.editGrandEvent.render(
				EDIT_FORM.fill(new GrandEventProps(event)), event, Organization.find.all(),
				EventTag.find.all()));
	}

	public Result doEdit(GrandEvent ge) {
		Form<GrandEventProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.grandevent.editGrandEvent.render(filledForm, ge,
					Organization.find.all(), EventTag.find.all()));
		}
		GrandEventProps geProps = filledForm.get();
		geProps.updateGrandEvent(ge);
		// TODO: if (1) start/end date changed and (2) this GE has some events
		// and (3) these events are out of start-end period, then:
		// 1. exclude out-of-period events from GE
		// 2. tell GE-editor about changes via flash message
		// 3. notify creators of excluded events via email (if creator !=
		// current GE-editor)
		return redirect(routes.GrandEventController.list());
	}

	public Result add() {
		return ok(views.html.grandevent.editGrandEvent.render(EDIT_FORM, null,
				Organization.find.all(), EventTag.find.all()));
	}

	@SubjectPresent
	public Result doAdd() {
		Form<GrandEventProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.grandevent.editGrandEvent.render(filledForm, null,
					Organization.find.all(), EventTag.find.all()));
		}
		GrandEventProps geProps = filledForm.get();
		GrandEvent ge = geProps.createGrandEvent();
		ge.author = ContextAugmenterAction.getLoggedUser();
		// TODO:
		// 1. count karma amount of creator
		// 2. according to it set published field
		// 3. if not published, then send admins/moderators invitation to review
		// event
		ge.save();
		return redirect(routes.GrandEventController.list());
	}

	// TODO: who is able to delete grand events? restrict access
	public Result remove(GrandEvent ge) {
		ge.delete();
		return ok();
	}

	public Result details(GrandEvent ge) {
		return TODO;
	}

	public static class GrandEventProps {

		public Long id;

		@Required
		public String name;

		@Required
		public String description;

		public LocalDate startDate;

		public LocalDate endDate;

		public List<EventTag> tags;

		public List<Organization> organizations;

		public GrandEventProps() {
			// no-op
		}

		public GrandEventProps(GrandEvent ge) {
			this.name = ge.name;
			this.description = ge.description;
			this.startDate = ge.startDate;
			this.endDate = ge.endDate;
			this.tags = ge.tags;
			this.organizations = ge.organizations;
		}

		private void setFields(GrandEvent ge) {
			ge.name = this.name;
			ge.description = this.description;
			ge.startDate = this.startDate;
			ge.endDate = this.endDate;
			ge.tags = this.tags;
			ge.organizations = this.organizations;
		}

		public GrandEvent createGrandEvent() {
			GrandEvent ge = new GrandEvent();
			setFields(ge);
			return ge;
		}

		public void updateGrandEvent(GrandEvent ge) {
			setFields(ge);
			ge.update();
		}
	}
}
