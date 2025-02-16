package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueueObject {
    private String queueName;
    private String processorClassName;
    private int maxSize;
    private int minSize;

    @JsonCreator
    public QueueObject(@JsonProperty("queueName") String queueName,
                       @JsonProperty("processorClassName") String processorClassName,
                       @JsonProperty("maxSize") int maxSize,
                       @JsonProperty("minSize") int minSize) {
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
