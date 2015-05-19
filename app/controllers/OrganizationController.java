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

	public static Result edit(Organization org) {
		return ok(views.html.organization.editOrganization.render(EDIT_FORM.fill(org), org));
	}

	public static Result doEdit(Organization oldOrg) {
		Form<Organization> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.organization.editOrganization.render(filledForm, oldOrg));
		}
		Organization org = filledForm.get();
		org.update(oldOrg.id);
		return redirect(routes.OrganizationController.list());
	}

	public static Result add() {
		return ok(views.html.organization.editOrganization.render(EDIT_FORM, null));
	}

	public static Result doAdd() {
		Form<Organization> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.organization.editOrganization.render(filledForm, null));
		}
		Organization org = filledForm.get();
		org.save();
		return redirect(routes.OrganizationController.list());
	}

	public static Result remove(Organization org) {
		org.delete();
		return ok();
	}

	public static Result details(Organization org) {
		return ok(views.html.organization.showOrganization.render(org));
	}
}
