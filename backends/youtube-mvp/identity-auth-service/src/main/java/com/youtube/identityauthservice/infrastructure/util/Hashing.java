package com.youtube.identityauthservice.infrastructure.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public final class Hashing {
    private Hashing() {}

    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sha256(String s) {
        return sha256(s.getBytes(StandardCharsets.UTF_8));
    }
}
