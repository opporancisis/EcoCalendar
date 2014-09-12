package controllers;

import models.organization.tag.OrganizationTag;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class OrganizationTagController extends Controller {

	private static final Form<OrganizationTag> EDIT_FORM = Form.form(OrganizationTag.class);

	public static Result list() {
		return ok(views.html.organization.listOrganizationTags.render(OrganizationTag.find.all()));
	}

	public static Result edit(long id) {
		OrganizationTag tag = OrganizationTag.find.byId(id);
		if (tag == null) {
			return Application.notFoundObject(OrganizationTag.class, id);
		}
		return ok(views.html.organization.editOrganizationTag.render(EDIT_FORM.fill(tag), tag));
	}

	public static Result doEdit(long id) {
		OrganizationTag tag = OrganizationTag.find.byId(id);
		if (tag == null) {
			return Application.notFoundObject(OrganizationTag.class, id);
		}
		Form<OrganizationTag> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.organization.editOrganizationTag.render(filledForm, tag));
		}
		OrganizationTag updatedTag = filledForm.get();
		updatedTag.update(id);
		return redirect(routes.OrganizationTagController.list());
	}

	public static Result create() {
		return ok(views.html.organization.editOrganizationTag.render(EDIT_FORM, null));
	}

	public static Result doCreate() {
		Form<OrganizationTag> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.organization.editOrganizationTag.render(filledForm, null));
		}
		OrganizationTag tag = filledForm.get();
		tag.save();
		return redirect(routes.OrganizationTagController.list());
	}

	public static Result remove(long id) {
		OrganizationTag tag = OrganizationTag.find.byId(id);
		if (tag == null) {
			return Application.notFoundObject(OrganizationTag.class, id);
		}
		tag.delete();
		return ok();
	}

}
