package com.youtube.common.domain.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for cryptographic hashing operations.
 */
public final class Hashing {
    private Hashing() {}

    /**
     * Computes SHA-256 hash of byte array.
     * 
     * @param input the input bytes
     * @return the hash bytes
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Computes SHA-256 hash of a string.
     * 
     * @param s the input string
     * @return the hash bytes
     */
    public static byte[] sha256(String s) {
        return sha256(s.getBytes(StandardCharsets.UTF_8));
    }
}

