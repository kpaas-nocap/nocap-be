package com.example.nocap.auth.service.mail;

public interface MailService {
    void send(String to, String subject, String body);
}
