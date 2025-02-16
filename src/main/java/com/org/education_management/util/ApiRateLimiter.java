package com.org.education_management.util;

import com.org.education_management.model.ApiRateLimit;
import com.org.education_management.tasks.ApiRateLimitTask;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private Map<Bucket, Bandwidth> bucketBandWidthMap = new HashMap<>();
    private final Map<String, Instant> lockPeriods = new ConcurrentHashMap<>();
    private static ApiRateLimiter apiRateLimiter = null;

    public static ApiRateLimiter getInstance(){
        if(apiRateLimiter == null){
            apiRateLimiter = new ApiRateLimiter();
        }
        return apiRateLimiter;
    }

    /**
     * Determines if the request is allowed based on rate-limiting rules.
     *
     * @param path     The endpoint path of the request.
     * @param clientIp The IP address of the client making the request.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean isAllowed(String path, String clientIp) {
        ApiRateLimit rule = ApiRateLimitTask.getRateLimitRule(path);
        if (rule == null) {
            return true; // Allow requests if no rate limit rule is configured
        }

        String key = generateKey(path, clientIp);

        // Check if the client is locked
        if (isLocked(key)) {
            return false;
        }

        // Check rate limit bucket
        buckets.compute(key, (k, existingBucket) -> {
            if (existingBucket == null || needsRefresh(existingBucket, rule)) {
                return createBucket(rule);
            }
            return existingBucket;
        });

        Bucket bucket = buckets.get(key);

        // If the bucket cannot consume tokens, initiate lock period
        if (!bucket.tryConsume(1)) {
            lockClient(key, rule.getLockPeriod());
            return false;
        }

        return true;
    }

    /**
     * Checks if a client is currently locked.
     *
     * @param key The unique key for the client and endpoint.
     * @return true if the client is locked, false otherwise.
     */
    private boolean isLocked(String key) {
        Instant lockExpiration = lockPeriods.get(key);
        if (lockExpiration == null) {
            return false;
        }
        if (lockExpiration.isBefore(Instant.now())) {
            lockPeriods.remove(key);
            return false;
        }
        return true;
    }

    /**
     * Locks a client for a specific period.
     *
     * @param key        The unique key for the client and endpoint.
     * @param lockPeriod The lock period in seconds.
     */
    private void lockClient(String key, int lockPeriod) {
        Instant lockExpiration = Instant.now().plus(Duration.ofSeconds(lockPeriod));
        lockPeriods.put(key, lockExpiration);
    }

    /**
     * Creates a new Bucket with the specified rate limit rule.
     *
     * @param rule The rate-limiting rule to configure the bucket.
     * @return A configured Bucket.
     */
    private Bucket createBucket(ApiRateLimit rule) {
        Bandwidth limit = Bandwidth.classic(
                rule.getLimit(),
                Refill.greedy(rule.getLimit(), Duration.ofSeconds(rule.getWindow()))
        );
        Bucket bucket = Bucket.builder().addLimit(limit).build();
        bucketBandWidthMap.put(bucket, limit);
        return bucket;
    }

    /**
     * Checks if an existing bucket needs to be refreshed based on new rules.
     *
     * @param bucket The existing bucket.
     * @param rule   The latest rate limit rule.
     * @return true if the bucket needs to be refreshed, false otherwise.
     */
    private boolean needsRefresh(Bucket bucket, ApiRateLimit rule) {
        Bandwidth bandwidth = bucketBandWidthMap.get(bucket);
        return bandwidth.getCapacity() != rule.getLimit() ||
                bandwidth.getRefillPeriodNanos() != Duration.ofSeconds(rule.getWindow()).toNanos();
    }

    /**
     * Generates a unique key for the bucket and lock state, combining path and client IP.
     *
     * @param path     The endpoint path of the request.
     * @param clientIp The IP address of the client making the request.
     * @return A unique key.
     */
    private String generateKey(String path, String clientIp) {
        return path + ":" + clientIp;
    }
}

