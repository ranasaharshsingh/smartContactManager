package com.springboot.smartContactManager.entities;

import org.springframework.stereotype.Component;



@Component
public class Email {
    private String to;
    private String from;
    private String title;
    private String text;

    public Email() {
    }

    public Email(String to, String from, String title, String text) {
        this.to = to;
        this.from = from;
        this.title = title;
        this.text = text;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Email [to=" + to + ", from=" + from + ", title=" + title + ", text=" + text + "]";
    }
}

