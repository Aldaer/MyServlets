package model.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Generates a list of supported timezones and their descriptions
 */
public class TimeZoneNames {
    public Properties getSupportedTimeZones() {
        return supportedTimeZones.get(currentLang);
    }

    public TimeZoneNames(@NotNull String lang) {
        currentLang = lang.toLowerCase();
        if (!supportedTimeZones.containsKey(currentLang)) {                     // This language wasn't requested before and wasn't read as a fallback
            Locale langLocale = new Locale(currentLang);
            ResourceBundle R = ResourceBundle.getBundle("timezones", langLocale);
            String resourceLang = R.getLocale().getLanguage();                  // Detect locale fallbacks, use the same Properties instead of creating a new one
            if (supportedTimeZones.containsKey(resourceLang)) supportedTimeZones.put(currentLang, supportedTimeZones.get(resourceLang));
            else {
                Properties langProps = new Properties();                       // Add new language to cache, load K-V pairs from file into cache map
                R.keySet().forEach(s -> langProps.put(s, R.getString(s)));
                supportedTimeZones.put(currentLang, langProps);                // lang and resourceLang are equivalent, but not guaranteed to be equal
                if (!currentLang.equals(resourceLang)) supportedTimeZones.put(resourceLang, langProps);
            }
        }
    }

    private static final Map<String, Properties> supportedTimeZones = new HashMap<>();      // Gets filled by request, used as cache

    private final String currentLang;
}
