package models.organization.tag;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import play.data.format.Formatters;

@SuppressWarnings("rawtypes")
public class OrganizationTagsListAnnotationFormatter extends
		Formatters.AnnotationFormatter<OrganizationTags, List> {
	@Override
	public List<OrganizationTag> parse(OrganizationTags annotation, String text, Locale locale)
			throws ParseException {
		List<OrganizationTag> result = new ArrayList<>();
		for (String idStr : text.split(",")) {
			long id = Long.parseLong(idStr);
			OrganizationTag file = OrganizationTag.find.byId(id);
			if (file == null) {
				throw new ParseException("unknown file: " + id, 0);
			}
			result.add(file);
		}
		return result;
	}

	@Override
	public String print(OrganizationTags annotation, List value, Locale locale) {
		@SuppressWarnings("unchecked")
		List<OrganizationTag> tags = (List<OrganizationTag>) value;
		return tags.stream().map(t -> "" + t.id).collect(Collectors.joining(","));
	}
}