package model;

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
    public MyTimer(String language, String timeZone) {
        locale = Locale.forLanguageTag(language);
        dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.FULL).withLocale(locale).withZone(ZoneId.of(timeZone, ZoneId.SHORT_IDS));

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
