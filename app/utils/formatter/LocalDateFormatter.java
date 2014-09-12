package utils.formatter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import play.data.format.Formatters.SimpleFormatter;

public final class LocalDateFormatter extends SimpleFormatter<LocalDate> {

	public static final String DAY_FORMAT = "dd.MM.yyyy";

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
			.ofPattern(DAY_FORMAT);

	@Override
	public LocalDate parse(String input, Locale l) throws ParseException {
		try {
			return LocalDate.parse(input, FORMATTER);
		} catch (DateTimeParseException e) {
			throw new ParseException("No valid Input", 0);
		}
	}

	@Override
	public String print(LocalDate localDate, Locale l) {
		return localDate.format(FORMATTER);
	}
}