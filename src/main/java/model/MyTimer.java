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
    }

    public String toString() {
        return String.format(locale, "Current time is %tH:%<tM:%<tS %<tZ %<td.%<tm.%<ty\n", Calendar.getInstance(tz, this.locale));
    }
    public Date getDate() {
        return Calendar.getInstance(tz, this.locale).getTime();
    }


    public String getTz() {
        return tz.getID();
    }

    private final TimeZone tz;
    private final Locale locale;

}
