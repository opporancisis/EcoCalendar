package controllers;

import java.io.File;
import java.io.IOException;

import models.file.UploadedFile;
import models.user.RoleName;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class FileController extends Controller {

	private static final Form<UploadedFile> EDIT_FORM = Form
			.form(UploadedFile.class);

	private static final ALogger log = Logger.of(FileController.class);

	@Restrict({ @Group(RoleName.ADMIN) })
	public static Result list() {
		return ok(views.html.file.listFiles.render(UploadedFile.find.all()));
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public static Result edit(long id) {
		UploadedFile file = UploadedFile.find.byId(id);
		if (file == null) {
			return Application.notFoundObject(UploadedFile.class, id);
		}
		Form<UploadedFile> filledForm = EDIT_FORM.fill(file);
		return ok(views.html.file.editFile.render(filledForm, file));
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public static Result doEdit(long id) {
		UploadedFile origFile = UploadedFile.find.byId(id);
		if (origFile == null) {
			return Application.notFoundObject(UploadedFile.class, id);
		}
		Form<UploadedFile> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			filledForm.data().put("id", Long.toString(id));
			filledForm.data().put("originalName", origFile.name);
			return badRequest(views.html.file.editFile.render(filledForm,
					origFile));
		}
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedFile = body.getFile("uploadedFile");
		UploadedFile file = filledForm.get();
		if (origFile.name.equals(file.name) && uploadedFile == null) {
			return redirect(routes.FileController.list());
		}
		file.modified = new DateTime();
		file.id = id;
		if (uploadedFile != null) {
			file.mime = uploadedFile.getContentType();
			File backup = new File(file.file().getAbsolutePath() + "_backup");
			try {
				// TODO: make this manipulations safer
				backup.delete();
				FileUtils.moveFile(file.file(), backup);
				FileUtils.moveFile(uploadedFile.getFile(), file.file());
				if (file.preview().exists()) {
					File previewBackup = new File(file.preview()
							.getAbsolutePath() + "_backup");
					previewBackup.delete();
					FileUtils.moveFile(file.preview(), previewBackup);
				}
				generatePreview(file);
			} catch (IOException e) {
				log.debug("Oops", e);
				return internalServerError();
			}
		}
		file.update(id);
		return redirect(routes.FileController.list());
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public static Result create() {
		return ok(views.html.file.editFile.render(EDIT_FORM, null));
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public static Result doCreate() {
		Form<UploadedFile> filledForm = EDIT_FORM.bindFromRequest();
		ObjectNode result = Json.newObject();
		if (filledForm.hasErrors()) {
			if (request().accepts("text/html")) {
				return badRequest(views.html.file.editFile.render(filledForm,
						null));
			} else {
				result.put("status", "error");
				result.put("errors", filledForm.errorsAsJson());
				return badRequest(result);
			}
		}

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedFile = body.getFile("uploadedFile");
		if (uploadedFile == null) {
			String err = Messages.get("error.need.file");
			if (request().accepts("text/html")) {
				flash(Application.FLASH_ERROR_KEY, err);
				return badRequest(views.html.file.editFile.render(filledForm,
						null));
			} else {
				result.put("status", "error");
				result.put("errors", err);
				return badRequest(result);
			}
		}
		String fileName = uploadedFile.getFilename();
		UploadedFile file = filledForm.get();
		if (StringUtils.isBlank(file.name)) {
			file.name = fileName;
		}
		file.created = new DateTime();
		file.modified = file.created;
		file.mime = uploadedFile.getContentType();
		file.save();
		try {
			if (file.file().exists()) {
				file.file().delete();
			}
			if (file.preview().exists()) {
				file.preview().delete();
			}
			FileUtils.moveFile(uploadedFile.getFile(), file.file());
			generatePreview(file);
		} catch (IOException e) {
			file.delete();
			log.debug("Oops", e);

			if (request().accepts("text/html")) {
				return internalServerError(e.getMessage());
			} else {
				result.put("status", "error");
				result.put("errors", e.getMessage());
				return internalServerError(result);
			}
		}

		if (request().accepts("text/html")) {
			return redirect(routes.FileController.list());
		} else {
			result.put("status", "ok");
			result.put("id", file.id);
			result.put("name", file.name);
			return ok(result);
		}
	}

	private static void generatePreview(UploadedFile file)
			throws ExecuteException, IOException {
		if (!file.isImage()) {
			return;
		}
		String convert = Play.application().configuration()
				.getString("napa.sys.imagemagick.convert");
		if (convert != null) {
			Executor exec = new DefaultExecutor();
			CommandLine cl = new CommandLine(convert);
			cl.addArguments(new String[] { file.file().getAbsolutePath(),
					"-resize", "80", file.preview().getAbsolutePath() });
			int exitvalue = exec.execute(cl);
			if (exitvalue != 0) {
				throw new IOException("bad exit code: " + exitvalue);
			}
		}
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public static Result remove(long id) {
		UploadedFile file = UploadedFile.find.byId(id);
		if (file == null) {
			return Application.notFoundObject(UploadedFile.class, id);
		}
		file.delete();
		return ok();
	}

	// TODO: no restriction - view anybody?
	public static Result get(long id) {
		UploadedFile file = UploadedFile.find.byId(id);
		if (file == null) {
			return Application.notFoundObject(UploadedFile.class, id);
		}
		response().setContentType(file.mime);
		response().setHeader(
				"Content-Disposition",
				"attachment; filename=\"" + FilenameUtils.getName(file.name)
						+ "\"");
		return ok(file.file());
	}

	public static Result preview(long id) {
		UploadedFile file = UploadedFile.find.byId(id);
		if (file == null) {
			return Application.notFoundObject(UploadedFile.class, id);
		}
		response().setContentType(file.mime);
		File preview = file.preview();
		return ok(preview);
	}
}
