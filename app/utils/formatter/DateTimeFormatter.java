package utils.formatter;

import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import play.data.format.Formatters.SimpleFormatter;

public final class DateTimeFormatter extends SimpleFormatter<DateTime> {
	private Pattern datePattern = Pattern
			.compile("([0-3]\\d)(?:-)([0-1]\\d)(?:-)(2\\d{3}) (\\d\\d):(\\d\\d):(\\d\\d)");

	@Override
	public DateTime parse(String input, Locale l) throws ParseException {
		Matcher m = datePattern.matcher(input);
		if (!m.find()) {
			throw new ParseException("No valid Input", 0);
		}
		int day = Integer.valueOf(m.group(1));
		int month = Integer.valueOf(m.group(2));
		int year = Integer.valueOf(m.group(3));
		int hour = Integer.valueOf(m.group(4));
		int mins = Integer.valueOf(m.group(5));
		int secs = Integer.valueOf(m.group(6));
		return new DateTime(year, month, day, hour, mins, secs);
	}

	@Override
	public String print(DateTime dt, Locale l) {
		return dt.toString("dd.MM.yyyy HH:mm:ss");
	}
}