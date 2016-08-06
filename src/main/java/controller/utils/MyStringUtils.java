package controller.utils;

import org.jetbrains.annotations.Nullable;

public class MyStringUtils {

    /**
     * Parses decimal string, returns null on any error.
     * Valid strings are '\-?\d+'
     *
     * @param s String to parse
     * @return Parsed Long, {@code null} on error
     */
    public static @Nullable Long parseOrNull(@Nullable String s) {
        if (s == null || s.equals("")) return null;
        char[] chars = s.toCharArray();
        Long result = 0L;
        boolean minus;
        int sti;
        if (chars[0] == '-') {
            minus = true;
            sti = 1;
        } else {
            minus = false;
            sti = 0;
        }
        for (; sti < chars.length; sti++) {
            char c = chars[sti];
            int v = (int) c - 48;
            if (v >= 0 && v <= 9) result = result * 10 + v;
            else return null;
        }
        return minus ? -result : result;
    }

    /**
     * Parses decimal string, returns defValue on any error.
     * Valid strings are '\-?\d+'
     *
     * @param s String to parse
     * @return Parsed long, or {@code defValue} on error
     */
    public static long parseOrDefault(@Nullable String s, long defValue) {
        Long p = parseOrNull(s);
        return p == null ? defValue : p;
    }

    /**
     * Parses decimal string, returns defValue on any error.
     * Valid strings are '\-?\d+'
     *
     * @param s String to parse
     * @return Parsed int, or {@code defValue} on error
     */
    public static int parseOrDefault(@Nullable String s, int defValue) {
        Long p = parseOrNull(s);
        if (p == null) return defValue;
        long lp = p;
        return (lp > Integer.MAX_VALUE) || (lp < Integer.MIN_VALUE) ? defValue : (int) lp;
    }

    /**
     * Returns long value closest to x within [min, max].     *
     * If x == null, min is returned.
     *
     * @return long value within bounds
     */
    public static long withinRangeOrMin(@Nullable Long x, long min, long max) {
        if (x == null) return min;
        long v = x;
        return (v < min) ? min : (v > max) ? max : v;
    }

    /**
     * Returns long value closest to x within [min, max].     *
     * If x == null, max is returned.
     *
     * @return long value within bounds
     */
    public static long withinRangeOrMax(@Nullable Long x, long min, long max) {
        if (x == null) return max;
        long v = x;
        return (v < min) ? min : (v > max) ? max : v;
    }

    private static final char[] ESCAPED_SQL = {'\'', '"', '\\', '%', '_'};
    private static final char ESCAPE_CHAR = '\\';

    public static String escapeSql(String sql) {
        if (sql == null) return null;
        final int sqlen = sql.length();
        char[] cbufIn = sql.toCharArray();
        char[] cbufOut = new char[sqlen * 2];
        int px = 0;
        for (int i = 0; i < sqlen; i++) {
            for (char sc : ESCAPED_SQL)
                if (cbufIn[i] == sc) {
                    cbufOut[px++] = ESCAPE_CHAR;
                    break;
                }
            cbufOut[px++] = cbufIn[i];
        }
        return new String(cbufOut, 0, px);
    }
}
