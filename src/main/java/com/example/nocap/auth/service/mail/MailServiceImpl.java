package com.example.nocap.auth.service.mail;

import com.example.nocap.auth.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender sender;
    public void send(String to, String subject, String body){
        SimpleMailMessage massage =new SimpleMailMessage();
        massage.setTo(to);
        massage.setSubject(subject);
        massage.setText(body);
        sender.send(massage);
    }
}