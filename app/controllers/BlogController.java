package controllers;

import java.util.List;

import models.blog.BlogPost;
import models.file.UploadedFile;
import models.user.RoleName;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class BlogController extends Controller {

	private static final Form<BlogPostProps> EDIT_FORM = Form.form(BlogPostProps.class);

	public Result list() {
		return ok(views.html.blog.listBlogPosts.render(BlogPost.find.query().order("created desc")
				.findList()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result add() {
		return ok(views.html.blog.editBlogPost.render(EDIT_FORM, null, UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result doAdd() {
		final Form<BlogPostProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.blog.editBlogPost.render(filledForm, null,
					UploadedFile.find.all()));
		}
		BlogPostProps postProps = filledForm.get();
		BlogPost post = postProps.createPost();
		post.owner = ContextAugmenterAction.getLoggedUser();
		post.save();
		return redirect(routes.BlogController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result edit(BlogPost post) {
		Form<BlogPostProps> filledForm = EDIT_FORM.fill(new BlogPostProps(post));
		return ok(views.html.blog.editBlogPost.render(filledForm, post, UploadedFile.find.all()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result doEdit(BlogPost post) {
		Form<BlogPostProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.blog.editBlogPost.render(filledForm, post,
					UploadedFile.find.all()));
		}
		BlogPostProps postProps = filledForm.get();
		postProps.updatePost(post);
		return redirect(routes.BlogController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result remove(BlogPost post) {
		post.delete();
		return ok();
	}

	public static class BlogPostProps {
		@Required
		public String title;

		@Required
		public String content;

		public List<UploadedFile> attachments;

		public BlogPostProps() {
			// no-op
		}

		public BlogPostProps(BlogPost post) {
			this.title = post.title;
			this.content = post.content;
			this.attachments = post.attachments;
		}

		private void setFields(BlogPost post) {
			post.title = this.title;
			post.content = this.content;
			post.attachments = this.attachments;
		}

		public void updatePost(BlogPost post) {
			setFields(post);
			post.update();
		}

		public BlogPost createPost() {
			BlogPost post = new BlogPost();
			setFields(post);
			return post;
		}

	}
}