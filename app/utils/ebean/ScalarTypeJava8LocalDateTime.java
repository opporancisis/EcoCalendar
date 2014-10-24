package utils.ebean;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.avaje.ebeaninternal.server.type.ScalarTypeBaseDateTime;

/**
 * ScalarType for Joda LocalDateTime. This maps to a JDBC Timestamp.
 */
public class ScalarTypeJava8LocalDateTime extends
		ScalarTypeBaseDateTime<LocalDateTime> {

	private static final ZoneOffset ZO = ZoneOffset.UTC;

	public ScalarTypeJava8LocalDateTime() {
		super(LocalDateTime.class, false, Types.TIMESTAMP);
	}

	@Override
	public LocalDateTime convertFromTimestamp(Timestamp ts) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime()), ZO);
	}

	@Override
	public Timestamp convertToTimestamp(LocalDateTime t) {
		return new Timestamp(t.atOffset(ZO).toInstant().toEpochMilli());
	}

	public Object toJdbcType(Object value) {
		if (value instanceof LocalDateTime) {
			return new Timestamp(((LocalDateTime) value).atOffset(ZO)
					.toInstant().toEpochMilli());
		}
		return BasicTypeConverter.toTimestamp(value);
	}

	public LocalDateTime toBeanType(Object value) {
		if (value instanceof java.util.Date) {
			return LocalDateTime.ofInstant(
					Instant.ofEpochMilli(((java.util.Date) value).getTime()),
					ZO);
		}
		return (LocalDateTime) value;
	}

	public LocalDateTime parseDateTime(long systemTimeMillis) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(systemTimeMillis),
				ZO);
	}

}
