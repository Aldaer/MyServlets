package model;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Model class
 */
public class MyTimer {
    public MyTimer(String locale, String timeZone) {
        this.locale = Locale.forLanguageTag(locale);
        tz = TimeZone.getTimeZone(timeZone);
        cal = Calendar.getInstance(tz, this.locale);
    }

    public String toString() {
        return String.format(locale, "Current time is: %tH:%<tM %<tZ %<td.%<tm.%<ty\n", cal);
    }
    public Date getDate() {
        return cal.getTime();
    }

    private final Locale locale;

    public String getTz() {
        return tz.getID();
    }

    private final TimeZone tz;
    private final Calendar cal;

}
