package com.org.education_management.model;

public class ApiRateLimit {

    private int limit; // Maximum number of requests allowed
    private int window; // Time window in seconds
    private int lockPeriod; // Lock period in seconds after exceeding the limit

    public ApiRateLimit(int limit, int window, int lockPeriod) {
        this.limit = limit;
        this.window = window;
        this.lockPeriod = lockPeriod;
    }

    // Getters and setters
    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public int getLockPeriod() {
        return lockPeriod;
    }

    public void setLockPeriod(int lockPeriod) {
        this.lockPeriod = lockPeriod;
    }
}

