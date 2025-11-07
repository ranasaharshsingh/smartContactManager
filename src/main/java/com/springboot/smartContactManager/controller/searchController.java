package com.springboot.smartContactManager.controller;

import org.springframework.web.bind.annotation.RestController;

import com.springboot.smartContactManager.Repository.ContactRepository;
import com.springboot.smartContactManager.Repository.UserRepository;
import com.springboot.smartContactManager.entities.Contact;
import com.springboot.smartContactManager.entities.User;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;



@RestController
@RequestMapping("/user")
public class searchController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(@PathVariable("query") String query,Principal principal) {
        System.out.println("query     :"+query);
        User user = this.userRepository.getUserByUserName(principal.getName());
        List<Contact> searchContacts= this.contactRepository.findByNameContainingAndUser(query, user);
        System.out.println("searched contacts ="+searchContacts);
        return ResponseEntity.ok(searchContacts);
    }
    
    
}
