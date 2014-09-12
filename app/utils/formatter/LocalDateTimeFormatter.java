package utils.formatter;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import play.data.format.Formatters.SimpleFormatter;

public final class LocalDateTimeFormatter extends
		SimpleFormatter<LocalDateTime> {

	public static final String FULL_DATE_FORMAT = LocalDateFormatter.DAY_FORMAT
			+ " HH:mm:ss";

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
			.ofPattern(FULL_DATE_FORMAT);

	@Override
	public LocalDateTime parse(String input, Locale l) throws ParseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String print(LocalDateTime dt, Locale l) {
		return dt.format(FORMATTER);
	}
}