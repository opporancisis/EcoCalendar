package utils;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import play.i18n.Messages;

public class TimeIntervalFormatter {

	private static final PeriodFormatter dhm = new PeriodFormatterBuilder()
			.appendDays()
			.appendSuffix(" " + Messages.get("label.time.short.day"))
			.appendSeparator(" ").appendHours()
			.appendSuffix(" " + Messages.get("label.time.short.hour"))
			.appendSeparator(" ").appendMinutes()
			.appendSuffix(" " + Messages.get("label.time.short.minute"))
			.appendSeparator(" ").appendSeconds()
			.appendSuffix(" " + Messages.get("label.time.short.second"))
			.toFormatter();

	public static String format(int seconds) {
		return dhm.print(new Period(Seconds.seconds(seconds))
				.normalizedStandard());
	}
}
