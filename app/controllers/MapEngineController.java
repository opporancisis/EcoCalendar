package controllers;

import models.geo.MapEngine;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class MapEngineController extends Controller {

	private static final Form<MapEngine> EDIT_FORM = Form.form(MapEngine.class);

	public static Result list() {
		return ok(views.html.geo.listMapEngines.render(MapEngine.find.all()));
	}

	public static Result edit(long id) {
		MapEngine engine = MapEngine.find.byId(id);
		if (engine == null) {
			return Application.notFoundObject(MapEngine.class, id);
		}
		return ok(views.html.geo.editMapEngine.render(EDIT_FORM.fill(engine), engine));
	}

	public static Result doEdit(long id) {
		MapEngine engine = MapEngine.find.byId(id);
		if (engine == null) {
			return Application.notFoundObject(MapEngine.class, id);
		}
		Form<MapEngine> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editMapEngine.render(filledForm, engine));
		}
		MapEngine updatedEngine = filledForm.get();
		updatedEngine.update(id);
		return redirect(routes.MapEngineController.list());
	}

	public static Result create() {
		return ok(views.html.geo.editMapEngine.render(EDIT_FORM, null));
	}

	public static Result doCreate() {
		Form<MapEngine> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.geo.editMapEngine.render(filledForm, null));
		}
		MapEngine engine = filledForm.get();
		engine.save();
		return redirect(routes.MapEngineController.list());
	}

	public static Result remove(long id) {
		MapEngine engine = MapEngine.find.byId(id);
		if (engine == null) {
			return Application.notFoundObject(MapEngine.class, id);
		}
		engine.delete();
		return ok();
	}

}
