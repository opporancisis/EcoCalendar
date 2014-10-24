package utils.formatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import models.organization.Organization;
import models.organization.Organizations;
import play.data.format.Formatters;

@SuppressWarnings("rawtypes")
public class OrganizationsListAnnotationFormatter extends
		Formatters.AnnotationFormatter<Organizations, List> {
	@Override
	public List<Organization> parse(Organizations annotation, String text, Locale locale)
			throws ParseException {
		List<Organization> result = new ArrayList<>();
		for (String idStr : text.split(",")) {
			long id = Long.parseLong(idStr);
			Organization org = Organization.find.byId(id);
			if (org == null) {
				throw new ParseException("unknown organization: " + id, 0);
			}
			result.add(org);
		}
		return result;
	}

	@Override
	public String print(Organizations annotation, List value, Locale locale) {
		@SuppressWarnings("unchecked")
		List<Organization> tags = (List<Organization>) value;
		return tags.stream().map(t -> "" + t.id).collect(Collectors.joining(","));
	}
}