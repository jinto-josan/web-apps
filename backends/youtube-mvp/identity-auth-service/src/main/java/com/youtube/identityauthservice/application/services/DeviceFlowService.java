package com.youtube.identityauthservice.application.services;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class DeviceFlowService {

    public enum Status { AUTHORIZATION_PENDING, APPROVED, EXPIRED }

    public static class DeviceStart {
        public final String deviceCode;
        public final String userCode;
        public final String verificationUri;
        public final long expiresInSeconds;
        public final long intervalSeconds;
        public DeviceStart(String deviceCode, String userCode, String verificationUri,
                           long expiresInSeconds, long intervalSeconds) {
            this.deviceCode = deviceCode;
            this.userCode = userCode;
            this.verificationUri = verificationUri;
            this.expiresInSeconds = expiresInSeconds;
            this.intervalSeconds = intervalSeconds;
        }
    }

    public static class PollResult {
        public final Status status;
        public final String approvedUserId; // when APPROVED
        public PollResult(Status status, String approvedUserId) {
            this.status = status;
            this.approvedUserId = approvedUserId;
        }
    }

    private final StringRedisTemplate redis;
    private final int userCodeLen;
    private final long deviceTtlSeconds;
    private final long pollIntervalSeconds;

    private final SecureRandom rnd = new SecureRandom();

    public DeviceFlowService(StringRedisTemplate redis, int userCodeLen, long deviceTtlSeconds, long pollIntervalSeconds) {
        this.redis = redis;
        this.userCodeLen = userCodeLen;
        this.deviceTtlSeconds = deviceTtlSeconds;
        this.pollIntervalSeconds = pollIntervalSeconds;
    }

    public DeviceStart start(String verificationUri, String clientId, String scope) {
        String deviceCode = randomUrlSafe(32);
        String userCode = humanCode(userCodeLen);
        String keyDev = keyDev(deviceCode);
        String keyUser = keyUser(userCode);

        long now = System.currentTimeMillis() / 1000L;
        String json = "{\"clientId\":\""+clientId+"\",\"scope\":\""+scope+"\",\"status\":\"pending\",\"interval\":"+pollIntervalSeconds+",\"iat\":"+now+"}";
        redis.opsForValue().set(keyDev, json, deviceTtlSeconds, TimeUnit.SECONDS);
        redis.opsForValue().set(keyUser, deviceCode, deviceTtlSeconds, TimeUnit.SECONDS);

        return new DeviceStart(deviceCode, userCode, verificationUri, deviceTtlSeconds, pollIntervalSeconds);
    }

    public PollResult poll(String deviceCode) {
        String state = redis.opsForValue().get(keyDev(deviceCode));
        if (!StringUtils.hasText(state)) {
            return new PollResult(Status.EXPIRED, null);
        }
        String approved = redis.opsForValue().get(keyApproved(deviceCode));
        if (!StringUtils.hasText(approved)) {
            return new PollResult(Status.AUTHORIZATION_PENDING, null);
        }
        return new PollResult(Status.APPROVED, approved);
    }

    public boolean verify(String userCode, String userId) {
        String deviceCode = redis.opsForValue().get(keyUser(userCode));
        if (!StringUtils.hasText(deviceCode)) return false;
        redis.opsForValue().set(keyApproved(deviceCode), userId, Duration.ofSeconds(deviceTtlSeconds));
        return true;
    }

    private String keyDev(String deviceCode) { return "dev:code:" + deviceCode; }
    private String keyUser(String userCode) { return "dev:user:" + userCode; }
    private String keyApproved(String deviceCode) { return "dev:approved:" + deviceCode; }

    private String randomUrlSafe(int bytesLen) {
        byte[] b = new byte[bytesLen];
        rnd.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private String humanCode(int len) {
        char[] alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet[rnd.nextInt(alphabet.length)]);
        }
        return sb.toString();
    }
}
