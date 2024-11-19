package com.org.education_management.service;

import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class CacheService {

    private static final Logger logger = Logger.getLogger(CacheService.class.getName());
    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager("TableCache");
    static CacheService cacheService = null;

    public static CacheService getInstance() {
        if(cacheService == null) {
            cacheService = new CacheService();
        }
        return cacheService;
    }

    // Put data into cache
    public void putInCache(String cacheName, String key, Object value) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if(isCacheAvailable(cacheName, key)) {
            if (cache != null) {
                cache.putIfAbsent(key, value);
            } else {
                throw new RuntimeException("Cache not found for name: " + cacheName);
            }
        } else {
            logger.log(Level.SEVERE, "CacheName : {0} Not found!", cacheName);
        }
    }

    // Get data from cache
    public Object getFromCache(String cacheName, String key) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache != null) {
            return cache.get(key, Object.class);
        }
        return null;
    }

    // Remove data from cache
    public void removeFromCache(String cacheName, String key) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evictIfPresent(key);
        }
    }

    // Check if cache exists
    public boolean isCacheAvailable(String cacheName, String key) {
        return caffeineCacheManager != null && caffeineCacheManager.getCache(cacheName) != null;
    }
}
