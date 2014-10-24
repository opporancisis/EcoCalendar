package controllers;

import models.organization.Organization;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class OrganizationController extends Controller {

	private static final Form<Organization> EDIT_FORM = Form.form(Organization.class);

	public static Result list() {
		return ok(views.html.organization.listOrganizations.render(Organization.find.all()));
	}

	public static Result edit(long id) {
		Organization org = Organization.find.byId(id);
		if (org == null) {
			return Application.notFoundObject(Organization.class, id);
		}
		return ok(views.html.organization.editOrganization.render(EDIT_FORM.fill(org), org));
	}

	public static Result doEdit(long id) {
		Form<Organization> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			Organization org = Organization.find.byId(id);
			if (org == null) {
				return Application.notFoundObject(Organization.class, id);
			}
			return badRequest(views.html.organization.editOrganization.render(filledForm, org));
		}
		Organization org = filledForm.get();
		org.update(id);
		return redirect(routes.OrganizationController.list());
	}

	public static Result create() {
		return ok(views.html.organization.editOrganization.render(EDIT_FORM, null));
	}

	public static Result doCreate() {
		Form<Organization> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.organization.editOrganization.render(filledForm, null));
		}
		Organization org = filledForm.get();
		org.save();
		return redirect(routes.OrganizationController.list());
	}

	public static Result remove(long id) {
		Organization org = Organization.find.byId(id);
		if (org == null) {
			return Application.notFoundObject(Organization.class, id);
		}
		org.delete();
		return ok();
	}

	public static Result details(long id) {
		return TODO;
	}
}
