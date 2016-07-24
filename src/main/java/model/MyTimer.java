package model;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Model class
 */
public class MyTimer {
    private static final ZoneId GMT = ZoneId.of("GMT");

    public MyTimer(String language, String timeZone) {
        locale = Locale.forLanguageTag(language);
        ZoneId z;
        try {
            z = ZoneId.of(timeZone, ZoneId.SHORT_IDS);
        } catch (DateTimeException e) {
            z = GMT;
            timeZone= "GMT";
        }

        dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.FULL).withLocale(locale).withZone(z);
        tz = TimeZone.getTimeZone(timeZone);
    }

    public String toString() {
        return dtf.format(ZonedDateTime.now());
    }
    public Date getDate() {
        return Calendar.getInstance(tz, locale).getTime();
    }

    public String getTz() {
        return tz.getID();
    }

    private final TimeZone tz;
    private final Locale locale;
    private final DateTimeFormatter dtf;

}
