package controllers;

import models.blog.BlogPost;
import models.file.UploadedFile;
import models.user.RoleName;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class BlogController extends Controller {

	private static final Form<BlogPost> EDIT_FORM = Form.form(BlogPost.class);

	public static Result list() {
		return ok(views.html.blog.listBlogPosts.render(BlogPost.find.query()
				.order("created desc").findList()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result add() {
		return ok(views.html.blog.editBlogPost.render(EDIT_FORM, null,
				UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result doAdd() {
		final Form<BlogPost> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.blog.editBlogPost.render(filledForm,
					null, UploadedFile.find.all()));
		}
		BlogPost post = filledForm.get();
		post.owner = ContextAugmenterAction.getLoggedUser();
		post.save();
		return redirect(routes.BlogController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result edit(BlogPost post) {
		Form<BlogPost> filledForm = EDIT_FORM.fill(post);
		return ok(views.html.blog.editBlogPost.render(filledForm, post,
				UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result doEdit(BlogPost oldPost) {
		Form<BlogPost> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.blog.editBlogPost.render(filledForm,
					oldPost, UploadedFile.find.all()));
		}
		BlogPost post = filledForm.get();
		post.update(oldPost.id);
		return redirect(routes.BlogController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public static Result remove(BlogPost post) {
		post.delete();
		return ok();
	}

}