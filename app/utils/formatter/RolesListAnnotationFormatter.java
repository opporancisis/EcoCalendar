package utils.formatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import models.user.RolesList;
import models.user.SecurityRole;

import org.apache.commons.lang3.StringUtils;

import play.data.format.Formatters;

@SuppressWarnings("rawtypes")
public class RolesListAnnotationFormatter extends
		Formatters.AnnotationFormatter<RolesList, List> {
	@Override
	public List<SecurityRole> parse(RolesList annotation, String text,
			Locale locale) throws ParseException {
		List<SecurityRole> result = new ArrayList<>();
		for (String role : text.split(",")) {
			result.add(SecurityRole.findByRoleName(role));
		}
		return result;
	}

	@Override
	public String print(RolesList annotation, List value, Locale locale) {
		return StringUtils.join(value, ",");
	}
}