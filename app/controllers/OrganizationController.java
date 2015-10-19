package controllers;

import javax.persistence.Lob;

import models.organization.Organization;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class OrganizationController extends Controller {

	private static final Form<OrganizationProps> EDIT_FORM = Form.form(OrganizationProps.class);

	public Result list() {
		return ok(views.html.organization.listOrganizations.render(Organization.find.all()));
	}

	public Result edit(Organization org) {
		return ok(views.html.organization.editOrganization.render(EDIT_FORM.fill(new OrganizationProps(org)), org));
	}

	public Result doEdit(Organization org) {
		Form<OrganizationProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.organization.editOrganization.render(filledForm, org));
		}
		OrganizationProps orgProps = filledForm.get();
		orgProps.updateOrg(org);
		return redirect(routes.OrganizationController.list());
	}

	public Result add() {
		return ok(views.html.organization.editOrganization.render(EDIT_FORM, null));
	}

	public Result doAdd() {
		Form<OrganizationProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.organization.editOrganization.render(filledForm, null));
		}
		OrganizationProps orgProps = filledForm.get();
		Organization org = orgProps.createOrg();
		org.save();
		return redirect(routes.OrganizationController.list());
	}

	public Result remove(Organization org) {
		org.delete();
		return ok();
	}

	public Result details(Organization org) {
		return ok(views.html.organization.showOrganization.render(org));
	}
	
	public static class OrganizationProps {
		
		public Long id;

		@Required
		public String name;

		// TODO: make rich-edit control for description
		@Required
		public String description;

		public OrganizationProps() {
			// no-op
		}
		
		public OrganizationProps(Organization org) {
			this.name = org.name;
			this.description = org.description;
		}
		
		private void setFields(Organization org) {
			org.name = this.name;
			org.description = this.description;
		}
		
		public void updateOrg(Organization org) {
			setFields(org);
			org.update();
		}
		
		public Organization createOrg() {
			Organization org = new Organization();
			setFields(org);
			return org;
		}
		
	}
}
