package utils.formatter;

import java.text.ParseException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import play.data.format.Formatters.SimpleFormatter;

public final class YearMonthFormatter extends SimpleFormatter<YearMonth> {

	private static final DateTimeFormatter YYYY_MM = DateTimeFormatter
			.ofPattern("MM-yyyy");

	@Override
	public YearMonth parse(String input, Locale l) throws ParseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String print(YearMonth localDate, Locale l) {
		return localDate.format(YYYY_MM);
	}
}