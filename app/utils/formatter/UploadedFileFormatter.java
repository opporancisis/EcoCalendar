package utils.formatter;

import java.text.ParseException;
import java.util.Locale;

import models.file.UploadedFile;
import play.data.format.Formatters.SimpleFormatter;

public class UploadedFileFormatter extends SimpleFormatter<UploadedFile> {
	@Override
	public UploadedFile parse(String text, Locale locale) throws ParseException {
		return UploadedFile.find.byId(Long.parseLong(text));
	}

	@Override
	public String print(UploadedFile t, Locale locale) {
		return "" + t.id;
	}
}