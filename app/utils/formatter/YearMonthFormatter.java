package utils.formatter;

import java.text.ParseException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import play.data.format.Formatters.SimpleFormatter;
import play.i18n.Messages;

public final class YearMonthFormatter extends SimpleFormatter<YearMonth> {

	private static final DateTimeFormatter YYYY_MM = DateTimeFormatter
			.ofPattern("MM-yyyy");

	@Override
	public YearMonth parse(String input, Locale l) throws ParseException {
		try {
			return YearMonth.parse(input, YYYY_MM);
		} catch (DateTimeParseException e) {
			throw new ParseException(Messages.get("error.format.must.be.MM-yyyy"), 0);
		}
	}

	@Override
	public String print(YearMonth localDate, Locale l) {
		return localDate.format(YYYY_MM);
	}
}