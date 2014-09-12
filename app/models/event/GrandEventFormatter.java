package models.event;

import java.text.ParseException;
import java.util.Locale;

import play.data.format.Formatters.SimpleFormatter;

public class GrandEventFormatter extends SimpleFormatter<GrandEvent> {
	@Override
	public GrandEvent parse(String text, Locale locale) throws ParseException {
		long id;
		try {
			id = Long.parseLong(text);
		} catch (NumberFormatException e) {
			throw new ParseException(text, 0);
		}
		return GrandEvent.find.byId(id);
	}

	@Override
	public String print(GrandEvent t, Locale locale) {
		return "" + t.id;
	}
}