package com.org.education_management.service;

import com.org.education_management.config.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.logging.Logger;

public class CacheService {
    public static final Logger logger = Logger.getLogger(CacheService.class.getName());
    public static CacheService cacheService = null;

    RedisConfig redisConfig = new RedisConfig();

    public static CacheService getInstance() {
        if(cacheService == null) {
            cacheService = new CacheService();
        }
        return cacheService;
    }

    @Autowired
    private CacheManager cacheManager;

    // Manually add a value to the cache
    public void addToCache(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    // Manually evict (delete) a value from the cache
    public void removeFromCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    // Manually clear all entries from the cache
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
