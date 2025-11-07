package com.springboot.smartContactManager.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Contact_Details")
public class Contact {
    
    @ManyToOne
    @JsonIgnore
    User user; 

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int customerId;
    private String name;
    private String nickName;
    private String email;
    private String work;
    private String imageUrl;
    private String phoneNumber;
    @Column(length = 5000)
    private String description;
    public Contact() {
    }
    public Contact(int customerId, String name, String email, String work, String nickName, String imageUrl,
            String phoneNumber, String description) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.work = work;
        this.nickName = nickName;
        this.imageUrl = imageUrl;
        this.phoneNumber = phoneNumber;
        this.description = description;
    }
    public int getCustomerId() {
        return customerId;
    }
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getWork() {
        return work;
    }
    public void setWork(String work) {
        this.work = work;
    }
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    @Override
    public String toString() {
        return "contact [customerId=" + customerId + ", name=" + name + ", email=" + email + ", work=" + work
                + ", nickName=" + nickName + ", imageUrl=" + imageUrl + ", phoneNumber=" + phoneNumber
                + ", description=" + description + "]";
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }


    
}
