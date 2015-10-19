package models.file;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.io.FilenameUtils;

import models.blog.BlogPost;
import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;

import com.avaje.ebean.Model;

import utils.Config;
import utils.IdPathBindable;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.google.common.base.Strings;

@Entity
public class UploadedFile extends Model implements IdPathBindable<UploadedFile> {

	private static final File FILES_DIR = Config.getConfig("files");
	static {
		Formatters.register(UploadedFile.class, new UploadedFileFormatter());
		if (!FILES_DIR.exists()) {
			FILES_DIR.mkdirs();
		}
	}

	@Id
	public Long id;

	public String name;

	@CreatedTimestamp
	public Date created;

	@UpdatedTimestamp
	public Date updated;

	public String mime;

	public static final Find<Long, UploadedFile> find = new Find<Long, UploadedFile>() {
	};

	private String getFileName() {
		return String.format("%019d", id);
	}

	public File file() {
		return new File(FILES_DIR, getFileName());
	}

	public File preview() {
		return new File(FILES_DIR, getFileName() + "p.png");
	}

	public boolean isImage() {
		return mime.startsWith("image/");
	}

	public String typeIcon() {
		String extension = FilenameUtils.getExtension(name);
		return "images/mime/" + (Strings.isNullOrEmpty(extension) ? "unknown" : extension) + ".png";
	}

	private static class UploadedFileFormatter extends
			SimpleFormatter<UploadedFile> {
		@Override
		public UploadedFile parse(String text, Locale locale)
				throws ParseException {
			long id;
			try {
				id = Long.parseLong(text);
			} catch (NumberFormatException e) {
				throw new ParseException(text, 0);
			}
			return UploadedFile.find.byId(id);
		}

		@Override
		public String print(UploadedFile t, Locale locale) {
			return "" + t.id;
		}
	}
}
