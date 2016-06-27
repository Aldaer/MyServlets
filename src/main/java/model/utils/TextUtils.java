package model.utils;

import java.nio.charset.StandardCharsets;

/**
 * Various utils
 */
public class TextUtils {
    public static String convertFromUTF8(String s) {
        byte[] sBytes = new byte[s.length() * 3];
        int i = 0;
        for (char c: s.toCharArray()) {
            sBytes[i++] = (byte)c;
        }
        return new String(sBytes, 0, i, StandardCharsets.UTF_8);
    }
}
