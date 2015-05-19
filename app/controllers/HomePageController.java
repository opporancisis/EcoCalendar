package controllers;

import java.util.List;

import models.blog.BlogPost;
import models.file.UploadedFile;
import models.standardPage.HomePage;
import models.standardPage.StandardPage;
import models.user.RoleName;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.Transactional;

import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class HomePageController extends Controller {

	private static final Form<HomePage> EDIT_FORM = Form.form(HomePage.class);

	public static Result index() {
		HomePage home = HomePage.get();
		List<BlogPost> news = BlogPost.find.query().orderBy("created desc")
				.setMaxRows(home.latestNewsMax).findList();
		return ok(views.html.standardPage.homePage.render(home, news));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result edit() {
		HomePage home = HomePage.get();
		Form<HomePage> filledForm = EDIT_FORM.fill(home);
		return ok(views.html.standardPage.editHomePage.render(filledForm, UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	@Transactional
	public static Result doEdit() {
		Form<HomePage> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.standardPage.editHomePage.render(filledForm,
					UploadedFile.find.all()));
		}
		HomePage homePage = filledForm.get();
		if (homePage.attachments == null) {
			Ebean.createUpdate(StandardPage.class, "update homePage set attachments=null")
					.execute();
		}
		homePage.update(HomePage.find.findUnique().id);
		return redirect(routes.HomePageController.index());
	}
}
