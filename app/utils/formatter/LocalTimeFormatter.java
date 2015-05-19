package utils.formatter;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import play.data.format.Formatters.SimpleFormatter;

public final class LocalTimeFormatter extends SimpleFormatter<LocalTime> {

	private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("HH:mm");

	@Override
	public LocalTime parse(String input, Locale l) throws ParseException {
		try {
			return LocalTime.parse(input, java.time.format.DateTimeFormatter.ISO_LOCAL_TIME);
		} catch (DateTimeParseException e) {
			throw new ParseException(input, 0);
		}
	}

	@Override
	public String print(LocalTime dt, Locale l) {
		return dt.format(dt.getSecond() == 0 ? SHORT
				: java.time.format.DateTimeFormatter.ISO_LOCAL_TIME);
	}
}