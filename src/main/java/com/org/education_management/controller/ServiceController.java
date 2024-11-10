package com.org.education_management.controller;

import com.org.education_management.MessageUtil.MessageSender;
import com.org.education_management.model.Message;
import com.org.education_management.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @PostMapping("/sendMail")
    public HashMap sendMail(@RequestBody Message message) {
        HashMap map = new HashMap<>();
        String recipient = "abhisheik94@gmail.com";
        String phoneNo = "+918524839275";
        String subject = "Alert: Test Email with HTML and Attachment";
        String htmlContent = "<h1>This is a test alert email</h1>"
                + "<p style='color:blue;'>This message includes an <b>attachment</b>.</p>";
        String attachmentPath = "C:\\Users\\HP\\Downloads\\file.txt";
        map = MessageSender.getInstance().sendMail(recipient, subject, htmlContent, attachmentPath);
        map = MessageSender.getInstance().sendText(phoneNo, htmlContent);
        return map;
    }
}