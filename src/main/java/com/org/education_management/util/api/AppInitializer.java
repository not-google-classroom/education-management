package com.org.education_management.util.api;

import com.org.education_management.config.SchedulerConfig;
import com.org.education_management.util.DynamicSchedulerUtil;

import java.util.HashMap;

public class AppInitializer {
    public static void startRateLimitScheduler() throws Exception{
        HashMap<String, SchedulerConfig> map = new HashMap<>();
        map.put("ApiRateLimitScheduler", new SchedulerConfig("*/60 * * * * *",
                -1, "ApiRateLimitTask"));
        DynamicSchedulerUtil.getInstance().addOrUpdateSchedulers(map);
    }
}
