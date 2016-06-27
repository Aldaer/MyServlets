package model.utils;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static java.lang.Math.random;

/**
 * Cryptographic stuff. Using standard implementation if SHA-256.
 */
//@SuppressWarnings("ALL")
@SuppressWarnings("WeakerAccess")
public class CryptoUtils {
    private static final char HEXCHAR[] = "0123456789abcdef".toCharArray();
    private static final MessageDigest md;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates SHA-256 hash for a UTF-8 string and returns it as a 32-byte array
     * @param s Input string
     * @return Hash byte array, always 32 bytes long
     */
    public static byte[] byteHash(@NotNull String s) {
        synchronized (md) {
            md.update(s.getBytes(UTF8));
            return md.digest();
        }
    }

    /**
     * Calculates SHA-256 hash for a UTF-8 string and returns it as a string of lowercase hexadecimal values
     * @param s Input string
     * @return Hash string, always 64 characters long
     */
    public static String stringHash(@NotNull String s) {
        byte[] hashbuf = byteHash(s);
        assert hashbuf.length == 32;
        return hexadecimate(hashbuf);
    }

    /**
     * Calculates SHA-256 hash of a string with added random 64-bit salt. The salt is returned as first 8 bytes of the array
     * @param s Input string
     * @return Hash byte array, always 40 bytes long, starting with 8-byte salt
     */
    public static byte[] byteRandomSaltedHash(@NotNull String s) {
        long saltHi = (long)((random() * 2 - 1) * Integer.MAX_VALUE);               // Actually, the random is 48-bit in this implementation, since two random() calls are not independent
        long saltLo = (long)((random() * 2 - 1) * Integer.MAX_VALUE);
        byte bSalt[] = { (byte)(saltHi >> 24), (byte)(saltHi >> 16), (byte)(saltHi >> 8), (byte)(saltHi),
                (byte)(saltLo >> 24), (byte)(saltLo >> 16), (byte)(saltLo >> 8), (byte)(saltLo) };
        return byteSaltedHash(bSalt, s);
    }

    /**
     * Calculates SHA-256 hash for a UTF-8 string with added random 64-bit salt and returns it as a string of lowercase hexadecimal values
     * @param s Input string
     * @return Hash string, always 80 characters long, starting with 16-character salt representation
     */
    public static String stringRandomSaltedHash(@NotNull String s) {
        byte[] hashbuf = byteRandomSaltedHash(s);
        return hexadecimate(hashbuf);
    }

    /**
     * Verifies if the string produces hash value equal to the reference one
     * @param sha SHA-256 hash to check
     * @param s String to check
     * @return true if hash values are equal, false otherwise
     */
    public static boolean verifyHash(byte[] sha, @NotNull String s) {
        assert sha.length == 32;
        byte[] hash = byteHash(s);
        return Arrays.equals(hash, sha);
    }

    /**
     * Verifies if the string produces hash value equal to the reference one
     * @param sha SHA-256 hash to check (as a hexadecimal string)(
     * @param s String to check
     * @return true if hash values are equal, false otherwise
     */
    public static boolean verifyHash(String sha, @NotNull String s) {
        assert sha.length() == 64;
        return verifyHash(unHexadecimate(sha), s);
    }


    /**
     * Verifies if 40-byte array string contains hash value equal to the salted hash of reference string
     * @param saltedsha 40-byte hash array (8 bytes salt followed by 32 byte SHA-256)
     * @param s String to check
     * @return true if hash values are equal, false otherwise
     */
    public static boolean verifySaltedHash(byte[] saltedsha, @NotNull String s) {
        assert saltedsha.length == 40;
        byte[] sha;
        byte[] sBytes = s.getBytes(UTF8);
        synchronized (md) {
            md.update(sBytes);
            md.update(saltedsha, 0, 8);
            sha = md.digest();
        }
        byte sha2[] = Arrays.copyOfRange(saltedsha, 8, 40);
        return Arrays.equals(sha, sha2);
    }

    /**
     * Verifies if 80-char String contains hash value equal to the hash of reference string
     * @param saltedshaS 80-char representation of hash (16 chars for salt followed by 64 chars for SHA-256, lowercase hexadecimal)
     * @param s String to check
     * @return true if hash values are equal, false otherwise
     */
    public static boolean verifySaltedHash(String saltedshaS, String s) {
        if (saltedshaS == null || saltedshaS.length() != 80 || s == null) return false;
        byte[] saltedSha = unHexadecimate(saltedshaS);
        return verifySaltedHash(saltedSha, s);
    }

    private static byte[] byteSaltedHash(byte[] salt, String s) {
        byte bSalted[] = Arrays.copyOf(salt, 40);
        byte sBytes[] = s.getBytes(UTF8);
        synchronized (md) {
            md.update(sBytes);
            md.update(salt);
            try {
                md.digest(bSalted, 8, 32);
            } catch (DigestException e) {
                throw new RuntimeException(e);
            }
        }
        return bSalted;
    }

    private static String hexadecimate(final byte b[]) {
        StringBuilder hash = new StringBuilder(b.length * 2);
        for (byte aB : b) hash.append(HEXCHAR[(aB >> 4) & 0xF]).append(HEXCHAR[aB & 0xF]);
        return hash.toString();
    }

    private static byte[] unHexadecimate(String s) {
        assert s.length() % 2 == 0;
        byte recov[] = new byte[s.length() / 2];
        char sChars[] = s.toCharArray();
        for (int i = 0; i < recov.length; i++) {
            int dhi = Character.digit(sChars[i * 2], 16);
            int dlo = Character.digit(sChars[i * 2 + 1], 16);
            recov[i] = (byte)((dhi * 16 + dlo)&0xff);
        }
        return recov;
    }

}
