package com.org.education_management.tasks;

import com.org.education_management.model.ApiRateLimit;
import com.org.education_management.model.ApiRule;
import com.org.education_management.util.api.ApiSecurityUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ApiRateLimitTask {
    private static Logger logger = Logger.getLogger(ApiRateLimitTask.class.getName());
    private static Map<String, ApiRateLimit> rateLimitRules = new ConcurrentHashMap<>();
    public void execute() {
        try {
            logger.log(Level.INFO, "Starting to load api rate limit properties at {0}"
                    , new Object[]{System.currentTimeMillis()});
            loadRateLimitConfig();
            logger.log(Level.INFO, "Rate limit configuration refreshed.");
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Exception while refreshing rate limit configuration", e);
        }
    }

    private void loadRateLimitConfig() throws Exception{
        List<ApiRule> apiRules = ApiSecurityUtil.getInstance().getApiRules();
        Optional.ofNullable(apiRules)
                .orElse(Collections.emptyList())
                .stream()
                .filter(rule -> rule.getPath() != null && rule.getRateLimit() != null)
                .forEach(rule -> rateLimitRules.put(rule.getPath(), rule.getRateLimit()));
    }

    public static ApiRateLimit getRateLimitRule(String path) {
        return rateLimitRules.get(path);
    }
}
