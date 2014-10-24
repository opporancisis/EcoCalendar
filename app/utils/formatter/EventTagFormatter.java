package utils.formatter;

import java.text.ParseException;
import java.util.Locale;

import models.event.tag.EventTag;
import play.data.format.Formatters.SimpleFormatter;

public class EventTagFormatter extends SimpleFormatter<EventTag> {
	@Override
	public EventTag parse(String text, Locale locale) throws ParseException {
		long id;
		try {
			id = Long.parseLong(text);
		} catch (NumberFormatException e) {
			throw new ParseException(text, 0);
		}
		return EventTag.find.byId(id);
	}

	@Override
	public String print(EventTag t, Locale locale) {
		return "" + t.id;
	}
}