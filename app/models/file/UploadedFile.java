package models.file;

import java.io.File;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.joda.time.DateTime;

import play.data.format.Formatters;
import play.db.ebean.Model;
import utils.Config;
import utils.formatter.AttachmentsListAnnotationFormatter;

@Entity
public class UploadedFile extends Model {

	private static final long serialVersionUID = 1L;

	private static final File FILES_DIR = Config.getConfig("files");
	static {
		Formatters.register(UploadedFile.class, new UploadedFileFormatter());
		Formatters.register(List.class, new AttachmentsListAnnotationFormatter());
		if (!FILES_DIR.exists()) {
			FILES_DIR.mkdirs();
		}
	}

	@Id
	public Long id;

	public String name;

	public DateTime created;

	public DateTime modified;

	public String mime;

	public static final Finder<Long, UploadedFile> find = new Finder<>(
			Long.class, UploadedFile.class);

	private String getFileName() {
		return String.format("%06d", id);
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
}
