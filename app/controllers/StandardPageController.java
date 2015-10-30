package controllers;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.file.UploadedFile;
import models.standardPage.StandardPage;
import models.user.RoleName;
import play.data.Form;
import play.data.validation.Constraints.Required;
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

	private static final Form<StandardPageProperties> EDIT_FORM = Form
			.form(StandardPageProperties.class);

	private static final Pattern IMG_SRC = Pattern
			.compile("(?:<img\\b(?:[^s>]+|\\Bs|s(?!rc\\s*=\\s*))+src\\s*=\\s*)[\"']?/file/(\\d+)\"");
	private static final Pattern A_HREF = Pattern
			.compile("(?:<a\\b(?:[^h>]+|\\Bh|h(?!ref\\s*=\\s*))+href\\s*=\\s*)[\"']?/file/(\\d+)\"");

	@Restrict({ @Group(RoleName.ADMIN), @Group(RoleName.USER) })
	public Result list() {
		return ok(views.html.standardPage.listStandardPages.render(StandardPage.find.query()
				.order("orderInd").findList()));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result edit(StandardPage page) {
		Form<StandardPageProperties> filledForm = EDIT_FORM.fill(new StandardPageProperties(page));
		return ok(views.html.standardPage.editStandardPage.render(filledForm, page));
	}

	@Restrict(@Group(RoleName.ADMIN))
	@Transactional
	public Result doEdit(StandardPage page) {
		Form<StandardPageProperties> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.standardPage.editStandardPage.render(filledForm, page));
		}
		StandardPageProperties pageProps = filledForm.get();
		pageProps.updatePage(page);
		page.attachments = parseContent(page.content);
		// if (page.attachments == null) {
		// Ebean.createUpdate(StandardPage.class,
		// "update standardPage set attachments=null where id=:id")
		// .setParameter("id", page.id).execute();
		// }
		page.update();
		Ebean.saveManyToManyAssociations(page, "attachments");
		return redirect(routes.StandardPageController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result add() {
		return ok(views.html.standardPage.editStandardPage.render(EDIT_FORM, null));
	}

	@Restrict(@Group(RoleName.ADMIN))
	public Result doAdd() {
		Form<StandardPageProperties> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.standardPage.editStandardPage.render(filledForm, null));
		}
		StandardPageProperties pageProps = filledForm.get();
		StandardPage page = pageProps.createPage();

		SqlQuery query = Ebean
				.createSqlQuery("select max(order_ind) + 1 as nextInd from standard_page");
		Long orderInd = query.findUnique().getLong("nextInd");
		page.orderInd = orderInd == null ? 1 : orderInd;
		page.save();
		return redirect(routes.StandardPageController.list());
	}

	@Restrict(@Group(RoleName.ADMIN))
	@Transactional
	public Result remove(StandardPage page) {
		page.delete();
		QueryIterator<StandardPage> it = StandardPage.find.query().orderBy("orderInd")
				.findIterate();
		for (long i = 1; it.hasNext(); i++) {
			StandardPage next = it.next();
			next.orderInd = i;
			next.update();
		}
		return ok();
	}

	public Result getStdPage(StandardPage page) {
		return ok(views.html.standardPage.viewStandardPage.render(page));
	}

	@Transactional
	@Restrict(@Group(RoleName.ADMIN))
	public Result moveBy(StandardPage page, long by) {
		StandardPage moved = StandardPage.find.query().where().eq("orderInd", page.orderInd + by)
				.findUnique();
		if (moved != null) {
			moved.orderInd = page.orderInd;
			page.orderInd += by;
			moved.update();
			page.update();
		}
		return redirect(routes.StandardPageController.list());
	}

	private Set<UploadedFile> parseContent(String content) {
		Set<UploadedFile> res = new HashSet<>();
		Matcher imgMatcher = IMG_SRC.matcher(content);
		while (imgMatcher.find()) {
			try {
				String fileIdStr = imgMatcher.group(1);
				long fileId = Long.parseLong(fileIdStr);
				res.add(UploadedFile.find.byId(fileId));
			} catch (NumberFormatException e) {
				// just skip
				// may be someone is hacking our system, passing too long digit
				// sequence...
				continue;
			}
		}
		Matcher aMatcher = A_HREF.matcher(content);
		while (aMatcher.find()) {
			try {
				String fileIdStr = aMatcher.group(1);
				long fileId = Long.parseLong(fileIdStr);
				res.add(UploadedFile.find.byId(fileId));
			} catch (NumberFormatException e) {
				// just skip
				// may be someone is hacking our system, passing too long digit
				// sequence...
				continue;
			}
		}
		return res;
	}

	public static class StandardPageProperties {

		public Boolean disabled;

		@Required
		public String title;
		
		public String link;

		@Required
		public String content;

		public StandardPageProperties() {
			// no-op
		}

		public StandardPageProperties(StandardPage page) {
			this.disabled = page.disabled;
			this.title = page.title;
			this.link = page.link;
			this.content = page.content;
		}

		private void setFields(StandardPage page) {
			page.disabled = this.disabled;
			page.title = this.title;
			page.link = this.link;
			page.content = this.content;
		}

		public void updatePage(StandardPage page) {
			setFields(page);
		}

		public StandardPage createPage() {
			StandardPage page = new StandardPage();
			setFields(page);
			return page;
		}

	}

}
