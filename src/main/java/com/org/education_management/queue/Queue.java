package com.org.education_management.queue;

import com.org.education_management.model.QueueObject;

import java.util.List;

public interface Queue {
    public void createQueue(QueueObject queueObject);

    public QueueObject getQueue(String queue);

    public void removeQueue(String queueName);
}
