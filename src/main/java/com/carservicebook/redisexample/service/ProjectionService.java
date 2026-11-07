package com.carservicebook.redisexample.service;

import com.carservicebook.redisexample.dto.ProjectionRequest;
import com.carservicebook.redisexample.dto.ProjectionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.carservicebook.cache.CacheKeyBuilder;
@Service
public class ProjectionService {
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);//jak długo czekający wątek cierpliwie czeka na wynik od “lidera”.
    private static final long POLL_INTERVAL_MS = 100;//co ile ms czekający sprawdza, czy wynik już jest
    private final StringRedisTemplate redis;
    private final Duration ttl;
    private final AtomicInteger computeCount = new AtomicInteger();


    public ProjectionService(StringRedisTemplate redis,
                             @Value("${projection.ttl-minutes}") long ttlMinutes) {
        this.redis = redis;
        this.ttl = Duration.ofMinutes(ttlMinutes);//po tym czasie Redis sam usunie blokadę. To zabezpieczenie na wypadek, gdyby aplikacja padła w trakcie liczenia i nie zdjęła blokady. Musi być dłuższy niż czas obliczeń (u nas 3 s).
    }

    public ProjectionResponse getProjection(ProjectionRequest request) {
        long start = System.currentTimeMillis();
        String key = CacheKeyBuilder.build(request.version(), request.list(), request.processId() );

        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            return new ProjectionResponse("HIT", key, start, cached);
        }

        String lockKey = "lock:" + key;
        String token = UUID.randomUUID().toString();
        Boolean acquired = redis.opsForValue().setIfAbsent(lockKey, token, ttl);

        if (Boolean.TRUE.equals(acquired)) {
            try {
                String again = redis.opsForValue().get(key);
                if (again != null) {
                    return new ProjectionResponse("HIT", key, start, again);
                }
                String result = compute(request);
                redis.opsForValue().set(key, result, ttl);
                return new ProjectionResponse("MISS", key, start, result);
            } finally {
                if (token.equals(redis.opsForValue().get(lockKey))) {
                    redis.delete(lockKey);
                }
            }
        }

        String awaited = waitForValue(key);
        if (awaited != null) {
            return new ProjectionResponse("WAITED", key, start, awaited);
        }

        String result = compute(request);
        redis.opsForValue().set(key, result, ttl);
        return new ProjectionResponse("MISS_FALLBACK", key, start, result);
    }

    private String waitForValue(String key) {
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            String value = redis.opsForValue().get(key);
            if (value != null) {
                return value;
            }
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    public int getComputeCount() {
        return computeCount.get();
    }

    private String compute(ProjectionRequest request) {
        computeCount.incrementAndGet();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return String.format("version=%s, processId=%s, result=%s",
                request.version(), request.processId(), "some result");
    }
}