package utils.formatter;

import java.text.ParseException;
import java.util.Locale;

import models.organization.Organization;
import play.data.format.Formatters.SimpleFormatter;

public class OrganizationFormatter extends SimpleFormatter<Organization> {
	@Override
	public Organization parse(String text, Locale locale) throws ParseException {
		return Organization.find.byId(Long.parseLong(text));
	}

	@Override
	public String print(Organization t, Locale locale) {
		return "" + t.id;
	}
}