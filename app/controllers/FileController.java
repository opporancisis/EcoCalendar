package controllers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import models.file.UploadedFile;
import models.user.RoleName;
import models.user.User;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import utils.JsonResponse;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class FileController extends Controller {

	private static final Form<UploadedFileProperties> EDIT_FORM = Form
			.form(UploadedFileProperties.class);

	@Restrict({ @Group(RoleName.ADMIN) })
	public Result list() {
		return ok(views.html.file.listFiles.render(UploadedFile.find.all()));
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public Result edit(UploadedFile file) {
		Form<UploadedFileProperties> filledForm = EDIT_FORM.fill(new UploadedFileProperties(file));
		return ok(views.html.file.editFile.render(filledForm, file));
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public Result doEdit(UploadedFile file) {
		Form<UploadedFileProperties> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			filledForm.data().put("originalName", file.name);
			return badRequest(views.html.file.editFile.render(filledForm, file));
		}
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedFile = body.getFile("uploadedFile");
		UploadedFileProperties ufProps = filledForm.get();
		if (file.name.equals(ufProps.name) && uploadedFile == null) {
			return redirect(routes.FileController.list());
		}
		ufProps.update(file);
		if (uploadedFile != null) {
			file.mime = uploadedFile.getContentType();
			File backup = new File(file.file().getAbsolutePath() + "_backup");
			try {
				// TODO: make this manipulations safer
				backup.delete();
				FileUtils.moveFile(file.file(), backup);
				FileUtils.moveFile(uploadedFile.getFile(), file.file());
				if (file.preview().exists()) {
					File previewBackup = new File(file.preview().getAbsolutePath() + "_backup");
					previewBackup.delete();
					FileUtils.moveFile(file.preview(), previewBackup);
				}
				generatePreview(file);
			} catch (IOException e) {
				Logger.debug("Oops", e);
				return internalServerError();
			}
		}
		file.update();
		return redirect(routes.FileController.list());
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public Result add() {
		return ok(views.html.file.editFile.render(EDIT_FORM, null));
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public Result doAdd() {
		Form<UploadedFileProperties> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			if (request().accepts("text/html")) {
				return badRequest(views.html.file.editFile.render(filledForm, null));
			} else {
				return badRequest(JsonResponse.err().set("errors", filledForm.errorsAsJson()));
			}
		}

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedFile = body.getFile("uploadedFile");
		if (uploadedFile == null) {
			String err = Messages.get("error.need.file");
			if (request().accepts("text/html")) {
				flash(Application.FLASH_ERROR_KEY, err);
				return badRequest(views.html.file.editFile.render(filledForm, null));
			} else {
				Map<String, String[]> errors = new HashMap<>();
				errors.put("uploadedFile", new String[] { err });
				return badRequest(JsonResponse.err().set("errors", Json.toJson(errors)));
			}
		}
		String fileName = uploadedFile.getFilename();
		UploadedFileProperties ufProps = filledForm.get();
		UploadedFile file = ufProps.create();
		if (StringUtils.isBlank(file.name)) {
			file.name = fileName;
		}
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
			Logger.debug("Oops", e);

			if (request().accepts("text/html")) {
				return internalServerError(e.getMessage());
			} else {
				return internalServerError(JsonResponse.err().put("errors", e.getMessage()));
			}
		}

		if (request().accepts("text/html")) {
			return redirect(routes.FileController.list());
		} else {
			return ok(JsonResponse.ok().put("id", file.id).put("name", file.name)
					.put("isImage", file.isImage()));
		}
	}

	private void generatePreview(UploadedFile file) throws ExecuteException, IOException {
		if (!file.isImage()) {
			return;
		}
		String convert = Play.application().configuration()
				.getString("napa.sys.imagemagick.convert");
		if (convert != null) {
			Executor exec = new DefaultExecutor();
			CommandLine cl = new CommandLine(convert);
			cl.addArguments(new String[] { file.file().getAbsolutePath(), "-resize", "x128",
					file.preview().getAbsolutePath() });
			int exitvalue = exec.execute(cl);
			if (exitvalue != 0) {
				throw new IOException("bad exit code: " + exitvalue);
			}
		}
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public Result remove(UploadedFile file) {
		file.delete();
		return ok();
	}

	public Result get(UploadedFile file) {
		File f = file.file();
		if (!f.exists()) {
			return Application.notFoundObject(UploadedFile.class, file.id);
		}
		response().setContentType(file.mime);
		response().setHeader("Content-Disposition",
				"attachment; filename=\"" + FilenameUtils.getName(file.name) + "\"");
		return ok(f);
	}

	public Result preview(UploadedFile file) {
		response().setContentType(file.mime);
		File preview = file.preview();
		return ok(preview);
	}

	@Restrict({ @Group(RoleName.ADMIN) })
	public Result attach() {
		return ok(views.html.file.attachFile.render(EDIT_FORM, UploadedFile.find.all()));
	}

	public static class UploadedFileProperties {
		public String name;

		public UploadedFileProperties() {
			// no-op
		}

		public UploadedFile create() {
			UploadedFile file = new UploadedFile();
			update(file);
			return file;
		}

		public UploadedFileProperties(UploadedFile uf) {
			this.name = uf.name;
		}

		public void update(UploadedFile uf) {
			uf.name = this.name;
		}
	}
}
