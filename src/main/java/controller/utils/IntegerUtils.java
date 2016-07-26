package controller.utils;

import org.jetbrains.annotations.Nullable;

public class IntegerUtils {

    /**
     * Parses decimal string, returns null on any error
     * @param s
     * @return
     */
    public static @Nullable Integer parseOrNull(@Nullable String s) {
        if (s == null || s.equals("")) return null;
        char[] chars = s.toCharArray();
        int result = 0;
        for (char c: chars) {
            int v = (int) c - 48;
            if (v >= 0 && v <= 9) result = result * 10 + v;
            else return null;
        }
        return result;
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
