package com.springboot.smartContactManager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.smartContactManager.entities.Email;
import com.springboot.smartContactManager.services.EmailService;


@RestController
@CrossOrigin
public class EmailController {
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/send-mail")
    public ResponseEntity<?> postsendMail(@RequestBody Email email) {
        boolean result = this.emailService.sendMail(email.getTo(),email.getFrom(), email.getTitle(), email.getText());
        if (result) {
            return ResponseEntity.ok("Mail sent successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to send mail");
        }
    }
}
