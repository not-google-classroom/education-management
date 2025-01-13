package com.org.education_management.config;

public class SchedulerConfig {
    private String cronString;
    private long maxRuns;

    private String taskClass;
    public SchedulerConfig(String cronString, long maxRuns, String taskClass) {
        this.cronString = cronString;
        this.maxRuns = maxRuns;
        this.taskClass = taskClass;
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

    public String getTaskClass() {
        return taskClass;
    }

    public void setTaskClass(String taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public String toString() {
        return "SchedulerConfig{" +
                "cronString='" + cronString + '\'' +
                ", maxRuns=" + maxRuns +
                ", taskClass='" + taskClass + '\'' +
                '}';
    }
}

