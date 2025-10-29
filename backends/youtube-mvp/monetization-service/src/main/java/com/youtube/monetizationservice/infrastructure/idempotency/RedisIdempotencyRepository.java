package com.youtube.monetizationservice.infrastructure.idempotency;

import com.youtube.monetizationservice.domain.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyRepository implements IdempotencyRepository {

	private final StringRedisTemplate redisTemplate;

	@Override
	public boolean store(String key, String result, Instant expiresAt) {
		Boolean set = redisTemplate.opsForValue().setIfAbsent(key, result == null ? "" : result,
			Duration.between(Instant.now(), expiresAt));
		return Boolean.TRUE.equals(set);
	}

	@Override
	public Optional<String> get(String key) {
		String value = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(value).filter(v -> !v.isEmpty());
	}

	@Override
	public void delete(String key) {
		redisTemplate.delete(key);
	}
}
