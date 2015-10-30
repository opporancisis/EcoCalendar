package controllers;

import java.util.List;

import models.blog.BlogPost;
import models.file.UploadedFile;
import models.standardPage.HomePage;
import models.user.RoleName;
import play.data.Form;
import play.data.validation.Constraints.Min;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.avaje.ebean.annotation.Transactional;

import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class HomePageController extends Controller {

	private static final Form<HomePageProps> EDIT_FORM = Form.form(HomePageProps.class);

	public Result index() {
		HomePage home = HomePage.get();
		List<BlogPost> news = BlogPost.find.query().orderBy("created desc")
				.setMaxRows(home.latestNewsMax).findList();
		return ok(views.html.standardPage.homePage.render(home, news));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result edit() {
		HomePage home = HomePage.get();
		Form<HomePageProps> filledForm = EDIT_FORM.fill(new HomePageProps(home));
		return ok(views.html.standardPage.editHomePage.render(filledForm, UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	@Transactional
	public Result doEdit() {
		Form<HomePageProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.standardPage.editHomePage.render(filledForm,
					UploadedFile.find.all()));
		}
		HomePageProps hpProps = filledForm.get();
		hpProps.updateHomePage(HomePage.get());
		return redirect(routes.HomePageController.index());
	}

	public static class HomePageProps {
		public String title;

		@Required
		public String content;

		public List<UploadedFile> attachments;

		@Min(0)
		public Integer latestNewsMax;

		public HomePageProps() {
			// no-op
		}

		public HomePageProps(HomePage hp) {
			this.title = hp.title;
			this.content = hp.content;
			this.attachments = hp.attachments;
			this.latestNewsMax = hp.latestNewsMax;
		}

		public void updateHomePage(HomePage hp) {
			hp.title = this.title;
			hp.content = this.content;
			hp.attachments = this.attachments;
			hp.latestNewsMax = this.latestNewsMax;
			hp.update();
		}

	}
}
