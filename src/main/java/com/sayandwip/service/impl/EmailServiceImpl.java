package com.sayandwip.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sayandwip.dto.EmailDetails;
import com.sayandwip.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    @Async
    public void sendEmailAlert(EmailDetails emailDetails) {
        validateEmailDetails(emailDetails);
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(senderEmail);
            mail.setTo(emailDetails.getRecipient());
            mail.setSubject(emailDetails.getSubject());
            mail.setText(emailDetails.getMessageBody());
            javaMailSender.send(mail);
            log.info("Email sent to {}", emailDetails.getRecipient());
        } catch (MailException e) {
            log.error("Error sending email: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendEmailWithAttachment(EmailDetails emailDetails) {
        validateEmailDetails(emailDetails);
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(senderEmail);
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getMessageBody(), false);
            attachFile(helper, emailDetails.getAttachment());
            javaMailSender.send(mimeMessage);
            log.info("Email with attachment sent to {}", emailDetails.getRecipient());
        } catch (MessagingException e) {
            log.error("Error sending email with attachment: {}", e.getMessage(), e);
        }
    }

    private void validateEmailDetails(EmailDetails emailDetails) {
        Objects.requireNonNull(emailDetails, "Email details must not be null");
        Objects.requireNonNull(emailDetails.getRecipient(), "Recipient must not be null");
        Objects.requireNonNull(emailDetails.getSubject(), "Subject must not be null");
        Objects.requireNonNull(emailDetails.getMessageBody(), "Message body must not be null");
    }

    private void attachFile(MimeMessageHelper helper, String filePath) throws MessagingException {
        if (filePath == null || filePath.isBlank()) return;
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("Attachment file not found: {}", filePath);
            return;
        }
        FileSystemResource resource = new FileSystemResource(file);
        helper.addAttachment(Objects.requireNonNull(resource.getFilename()), resource);
    }
}
