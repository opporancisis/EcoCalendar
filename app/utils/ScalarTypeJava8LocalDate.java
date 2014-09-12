package utils;

import java.sql.Date;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.avaje.ebeaninternal.server.type.ScalarTypeBaseDate;

public class ScalarTypeJava8LocalDate extends ScalarTypeBaseDate<LocalDate> {

	public ScalarTypeJava8LocalDate() {
		super(LocalDate.class, false, Types.DATE);
	}

	@Override
	public LocalDate convertFromDate(Date ts) {
		return Instant.ofEpochMilli(((java.util.Date) ts).getTime())
				.atZone(ZoneOffset.systemDefault()).toLocalDate();
	}

	@Override
	public Date convertToDate(LocalDate t) {
		return new java.sql.Date(t.atStartOfDay().atZone(ZoneOffset.systemDefault())
				.toEpochSecond() * 1000L);
	}

	public Object toJdbcType(Object value) {
		if (value instanceof LocalDate) {
			return new java.sql.Date(((LocalDate) value).atStartOfDay()
					.atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000L);
		}
		return BasicTypeConverter.toDate(value);
	}

	public LocalDate toBeanType(Object value) {
		if (value instanceof java.util.Date) {
			return Instant.ofEpochMilli(((java.util.Date) value).getTime())
					.atZone(ZoneOffset.systemDefault()).toLocalDate();
		}
		return (LocalDate) value;
	}

	public LocalDate parseDateTime(long systemTimeMillis) {
		return Instant.ofEpochMilli(systemTimeMillis).atZone(ZoneOffset.systemDefault())
				.toLocalDate();
	}

}
