package com.org.education_management.model;

public class DynamicQueue {
    private String queueName;
    private String processorClassName;
    private int maxSize;
    private int minSize;

    public DynamicQueue(String queueName, String processorClassName, int maxSize, int minSize) {
        this.queueName = queueName;
        this.processorClassName = processorClassName;
        this.maxSize = maxSize;
        this.minSize = minSize;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getProcessorClassName() {
        return processorClassName;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMinSize() {
        return minSize;
    }
}
