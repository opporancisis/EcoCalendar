package controllers;

import models.file.UploadedFile;
import models.standardPage.StandardPage;
import models.user.RoleName;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.annotation.Transactional;

import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class StandardPageController extends Controller {

	private static final Form<StandardPage> EDIT_FORM = Form
			.form(StandardPage.class);

	@Restrict({ @Group(RoleName.ADMIN), @Group(RoleName.USER) })
	public static Result list() {
		return ok(views.html.standardPage.listStandardPages
				.render(StandardPage.find.query().order("orderInd").findList()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result edit(long id) {
		StandardPage standardPage = StandardPage.find.byId(id);
		if (standardPage == null) {
			return Application.notFoundObject(StandardPage.class, id);
		}
		Form<StandardPage> filledForm = EDIT_FORM.fill(standardPage);
		return ok(views.html.standardPage.editStandardPage.render(filledForm,
				UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	@Transactional
	public static Result doEdit(long id) {
		Form<StandardPage> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			StandardPage standardPage = StandardPage.find.byId(id);
			if (standardPage == null) {
				return Application.notFoundObject(StandardPage.class, id);
			}
			filledForm.data().put("id", Long.toString(id));
			filledForm.data().put("originalName", standardPage.title);
			return badRequest(views.html.standardPage.editStandardPage.render(
					filledForm, UploadedFile.find.all()));
		}
		StandardPage standardPage = filledForm.get();
		if (standardPage.attachments == null) {
			Ebean.createUpdate(StandardPage.class,
					"update standardPage set attachments=null where id=:id")
					.setParameter("id", id).execute();
		}
		standardPage.update(id);
		return redirect(routes.StandardPageController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result create() {
		return ok(views.html.standardPage.editStandardPage.render(EDIT_FORM,
				UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result doCreate() {
		Form<StandardPage> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.standardPage.editStandardPage.render(
					filledForm, UploadedFile.find.all()));
		}
		StandardPage standardPage = filledForm.get();
		standardPage.disabled = false;

		SqlQuery query = Ebean
				.createSqlQuery("select max(order_ind) + 1 as nextInd from standard_page");
		Long orderInd = query.findUnique().getLong("nextInd");
		standardPage.orderInd = orderInd == null ? 1 : orderInd;
		standardPage.save();
		return redirect(routes.StandardPageController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result remove(long id) {
		StandardPage standardPage = StandardPage.find.byId(id);
		if (standardPage == null) {
			return Application.notFoundObject(StandardPage.class, id);
		}
		standardPage.delete();
		return ok();
	}

	public static Result getStdPageById(long id) {
		StandardPage page = StandardPage.find.byId(id);
		if (page == null) {
			return Application.notFoundObject(StandardPage.class, id);
		}
		return ok(views.html.standardPage.viewStandardPage.render(page));
	}

	public static Result getStdPage(String path) {
		StandardPage page = StandardPage.find.query().where()
				.eq("link", "/" + path).findUnique();
		if (page == null) {
			return Application.notFoundObject(StandardPage.class, path);
		}
		return ok(views.html.standardPage.viewStandardPage.render(page));
	}

	@Transactional
	@Restrict(@Group(RoleName.ADMIN))
	public static Result moveBy(long id, long by) {
		StandardPage page = StandardPage.find.byId(id);
		if (page == null) {
			return Application.notFoundObject(StandardPage.class, id);
		}
		StandardPage moved = StandardPage.find.query().where()
				.eq("orderInd", page.orderInd + by).findUnique();
		if (moved != null) {
			moved.orderInd = page.orderInd;
			page.orderInd += by;
			moved.update();
			page.update(id);
		}
		return redirect(routes.StandardPageController.list());
	}
}
