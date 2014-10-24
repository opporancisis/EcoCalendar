package utils.ebean;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.ScalarTypeBase;

/**
 * ScalarType for Joda LocalTime. This maps to a JDBC Time.
 */
public class ScalarTypeJava8LocalTime extends ScalarTypeBase<LocalTime> {

	public ScalarTypeJava8LocalTime() {
		super(LocalTime.class, false, Types.TIME);
	}

	public void bind(DataBind b, LocalTime value) throws SQLException {
		if (value == null) {
			b.setNull(Types.TIME);
		} else {
			b.setTime(Time.valueOf(value));
		}
	}

	public LocalTime read(DataReader dataReader) throws SQLException {
		Time sqlTime = dataReader.getTime();
		if (sqlTime == null) {
			return null;
		} else {
			return sqlTime.toLocalTime();
		}
	}

	public Object toJdbcType(Object value) {
		if (value instanceof LocalTime) {
			return Time.valueOf(((LocalTime) value));
		}
		return BasicTypeConverter.toTime(value);
	}

	public LocalTime toBeanType(Object value) {
		if (value instanceof java.util.Date) {
			Instant instant = Instant.ofEpochMilli(((java.util.Date) value).getTime());
			return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
		}
		return (LocalTime) value;
	}

	public String formatValue(LocalTime v) {
		return v.toString();
	}

	public LocalTime parse(String value) {
		return LocalTime.parse(value);
	}

	public LocalTime parseDateTime(long systemTimeMillis) {
		Instant instant = Instant.ofEpochMilli(systemTimeMillis);
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
	}

	public boolean isDateTimeCapable() {
		return true;
	}

	public Object readData(DataInput dataInput) throws IOException {
		if (!dataInput.readBoolean()) {
			return null;
		} else {
			String val = dataInput.readUTF();
			return parse(val);
		}
	}

	public void writeData(DataOutput dataOutput, Object v) throws IOException {
		Time value = (Time) v;
		if (value == null) {
			dataOutput.writeBoolean(false);
		} else {
			dataOutput.writeBoolean(true);
			dataOutput.writeUTF(format(value));
		}
	}
}
