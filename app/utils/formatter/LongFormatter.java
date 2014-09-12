package utils.formatter;

import java.text.ParseException;
import java.util.Locale;

import play.data.format.Formatters.SimpleFormatter;

public final class LongFormatter extends SimpleFormatter<Long> {

	@Override
	public Long parse(String input, Locale l) throws ParseException {
		return Long.valueOf(input);
	}

	@Override
	public String print(Long val, Locale l) {
		return val.toString();
	}
}