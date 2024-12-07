package com.org.education_management.config;

public class SchedulerConfig {
    private String cronString;
    private long maxRuns;

    public SchedulerConfig(String cronString, long maxRuns) {
        this.cronString = cronString;
        this.maxRuns = maxRuns;
    }

    public String getCronString() {
        return cronString;
    }

    public void setCronString(String cronString) {
        this.cronString = cronString;
    }

    public Long getMaxRuns() {
        return maxRuns;
    }

    public void setMaxRuns(long maxRuns) {
        this.maxRuns = maxRuns;
    }

    @Override
    public String toString() {
        return "SchedulerConfig{" +
                "cronString='" + cronString + '\'' +
                ", maxRuns=" + maxRuns +
                '}';
    }
}

