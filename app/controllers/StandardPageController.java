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
import com.avaje.ebean.QueryIterator;
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
	public static Result edit(StandardPage page) {
		Form<StandardPage> filledForm = EDIT_FORM.fill(page);
		return ok(views.html.standardPage.editStandardPage.render(filledForm,
				page, UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	@Transactional
	public static Result doEdit(StandardPage oldPage) {
		Form<StandardPage> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.standardPage.editStandardPage.render(
					filledForm, oldPage, UploadedFile.find.all()));
		}
		StandardPage standardPage = filledForm.get();
		if (standardPage.attachments == null) {
			Ebean.createUpdate(StandardPage.class,
					"update standardPage set attachments=null where id=:id")
					.setParameter("id", oldPage.id).execute();
		}
		standardPage.update(oldPage.id);
		return redirect(routes.StandardPageController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result add() {
		return ok(views.html.standardPage.editStandardPage.render(EDIT_FORM,
				null, UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result doAdd() {
		Form<StandardPage> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.standardPage.editStandardPage.render(
					filledForm, null, UploadedFile.find.all()));
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
	@Transactional
	public static Result remove(StandardPage page) {
		page.delete();
		QueryIterator<StandardPage> it = StandardPage.find.query()
				.orderBy("orderInd").findIterate();
		for (long i = 1; it.hasNext(); i++) {
			StandardPage next = it.next();
			next.orderInd = i;
			next.update();
		}
		return ok();
	}

	public static Result getStdPageById(StandardPage page) {
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
	public static Result moveBy(StandardPage page, long by) {
		StandardPage moved = StandardPage.find.query().where()
				.eq("orderInd", page.orderInd + by).findUnique();
		if (moved != null) {
			moved.orderInd = page.orderInd;
			page.orderInd += by;
			moved.update();
			page.update(page.id);
		}
		return redirect(routes.StandardPageController.list());
	}

}
