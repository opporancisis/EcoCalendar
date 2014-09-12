package models.organization.tag;

import java.text.ParseException;
import java.util.Locale;

import play.data.format.Formatters.SimpleFormatter;

public class OrganizationTagFormatter extends SimpleFormatter<OrganizationTag> {
	@Override
	public OrganizationTag parse(String text, Locale locale) throws ParseException {
		return OrganizationTag.find.byId(Long.parseLong(text));
	}

	@Override
	public String print(OrganizationTag t, Locale locale) {
		return "" + t.id;
	}
}