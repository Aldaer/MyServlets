package model.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class CryptoUtilsTest {
    private final String SHA_empty = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final String SHA_abc = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @Test
    public void testStringHash() throws Exception {
        assertEquals(CryptoUtils.stringHash(""), SHA_empty);
        assertEquals(CryptoUtils.stringHash("abc"), SHA_abc);
    }

    @Test
    public void testSaltedHash() throws Exception {
        String s1 = CryptoUtils.stringRandomSaltedHash("test");
        String s2 = CryptoUtils.stringRandomSaltedHash("test");
        assertFalse(s1.equals(s2));                         // 1/2^64 chance to fail
    }

    @Test
    public void testHashVerification() throws Exception {
        String s1 = CryptoUtils.stringHash("test");
        assertTrue(CryptoUtils.verifyHash(s1, "test"));
    }

    @Test
    public void testSaltedVerification() throws Exception {
        String s1 = CryptoUtils.stringRandomSaltedHash("test");
        assertTrue(CryptoUtils.verifySaltedHash(s1, "test"));
    }

    @Test
    public void name() throws Exception {
        System.out.println("Вася = " + CryptoUtils.stringRandomSaltedHash("qwerty"));
        System.out.println("Петя = " + CryptoUtils.stringRandomSaltedHash("qwerty"));
        System.out.println("Миша = " + CryptoUtils.stringRandomSaltedHash("qwerty"));
    }
}