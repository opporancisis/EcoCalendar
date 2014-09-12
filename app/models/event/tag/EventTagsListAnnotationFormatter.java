package models.event.tag;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import play.data.format.Formatters;

@SuppressWarnings("rawtypes")
public class EventTagsListAnnotationFormatter extends
		Formatters.AnnotationFormatter<EventTagsList, List> {

	@Override
	public List<EventTag> parse(EventTagsList annotation, String text, Locale locale)
			throws ParseException {
		List<EventTag> result = new ArrayList<>();
		for (String idStr : text.split(",")) {
			long id = Long.parseLong(idStr);
			result.add(EventTag.find.byId(id));
		}
		return result;
	}

	@Override
	public String print(EventTagsList annotation, List value, Locale locale) {
		@SuppressWarnings("unchecked")
		List<EventTag> tags = (List<EventTag>) value;
		return tags.stream().map(t -> "" + t.id).collect(Collectors.joining(","));
	}
}