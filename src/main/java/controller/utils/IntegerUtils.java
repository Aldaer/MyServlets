package controller.utils;

import org.jetbrains.annotations.Nullable;

public class IntegerUtils {

    /**
     * Parses decimal string, returns null on any error.
     * Valid strings are '\-?\d+'
     * @param s String to parse
     * @return Parsed int, null on error
     */
    public static @Nullable Integer parseOrNull(@Nullable String s) {
        if (s == null || s.equals("")) return null;
        char[] chars = s.toCharArray();
        int result = 0;
        int sti = 0;
        boolean minus = false;
        if (chars[0] == '-') {
            minus = true;
            sti = 1;
        }
        for (; sti < chars.length; sti++) {
            char c = chars[sti];
            int v = (int) c - 48;
            if (v >= 0 && v <= 9) result = result * 10 + v;
            else return null;
        }
        return minus? -result : result;
    }

    /**
     * Returns int value closest to x within [min, max].     *
     * If x == null, min is returned.
     * @return int value within bounds
     */
    public static int withinRangeOrMin(@Nullable Integer x, int min, int max) {
        if (x == null) return min;
        int v = x;
        return (v < min)? min : (v > max)? max : v;
    }

    /**
     * Returns int value closest to x within [min, max].     *
     * If x == null, max is returned.
     * @return int value within bounds
     */
    public static int withinRangeOrMax(@Nullable Integer x, int min, int max) {
        if (x == null) return max;
        int v = x;
        return (v < min)? min : (v > max)? max : v;
    }
}
