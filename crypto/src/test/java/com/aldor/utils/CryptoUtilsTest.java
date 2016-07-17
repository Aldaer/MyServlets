package com.aldor.utils;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class CryptoUtilsTest {
    private final String SHA_empty = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final String SHA_abc = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @Test
    public void testStringHash() throws Exception {
        assertThat(CryptoUtils.stringHash(""), is(SHA_empty));
        assertThat(CryptoUtils.stringHash("abc"), is(SHA_abc));
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
        assertThat(CryptoUtils.verifyHash(s1, "test"), is(true));
    }

    @Test
    public void testSaltedVerification() throws Exception {
        String s1 = CryptoUtils.stringRandomSaltedHash("test");
        assertThat(CryptoUtils.verifySaltedHash(s1, "test"), is(true));
    }

    @Test
    public void digestPasswords() throws Exception {
        System.out.println("Вася = " + CryptoUtils.stringRandomSaltedHash("12345"));
        System.out.println("Петя = " + CryptoUtils.stringRandomSaltedHash("12345"));
        System.out.println("Миша = " + CryptoUtils.stringRandomSaltedHash("12345"));

        System.out.println("admin = " + CryptoUtils.stringRandomSaltedHash("123"));

    }
}