package com.org.education_management.controller;

import com.org.education_management.model.Message;
import com.org.education_management.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ServiceController {

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping("/postMessage")
    public Message saveMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }

    @GetMapping("/getMessage")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }
}