package com.socialmedia.togetherly.service;

public interface EmailService {
    void sendHtmlEmail(String to, String subject, String htmlBody);
}
