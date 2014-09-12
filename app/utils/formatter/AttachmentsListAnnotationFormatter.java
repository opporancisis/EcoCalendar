package utils.formatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import models.file.UploadedFile;
import models.file.UploadedFiles;
import play.data.format.Formatters;

public class AttachmentsListAnnotationFormatter extends
		Formatters.AnnotationFormatter<UploadedFiles, List> {
	@Override
	public List<UploadedFile> parse(UploadedFiles annotation, String text,
			Locale locale) throws ParseException {
		List<UploadedFile> result = new ArrayList<>();
		for (String idStr : text.split(",")) {
			long id = Long.parseLong(idStr);
			UploadedFile file = UploadedFile.find.byId(id);
			if (file == null) {
				throw new ParseException("unknown file: " + id, 0);
			}
			result.add(file);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String print(UploadedFiles annotation, List value, Locale locale) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (UploadedFile file : (List<UploadedFile>) value) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(file.id);
		}
		return sb.toString();
	}
}