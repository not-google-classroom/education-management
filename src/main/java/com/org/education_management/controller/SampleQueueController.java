package com.org.education_management.controller;

import com.org.education_management.queue.kafka.KafkaQueue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("api")
public class SampleQueueController {
    @GetMapping("/sendQueue")
    public HashMap sendQueue() {
        KafkaQueue queue = new KafkaQueue();
        queue.send("sample", "iam in queue");
        return null;
    }
}
