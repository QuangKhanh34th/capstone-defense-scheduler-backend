package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.EmailRequest;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final Resend resend;
    private final String defaultFromEmail;

    // 1. Inject the Resend bean and the default email value
    public EmailService(
            Resend resend, // <--- Spring will now inject the bean here automatically!
            @Value("${resend.from.email:noreply@labmanagement.online}") String defaultFromEmail) {
        this.resend = resend;
        this.defaultFromEmail = defaultFromEmail;
    }

    // 2. Convenience method
    public void sendMail(String to, String subject, String content) {
        EmailRequest request = EmailRequest.builder()
                .to(List.of(to))
                .subject(subject)
                .htmlContent(content)
                .build();
        sendMail(request);
    }

    // 3. Main flexible method
    public void sendMail(EmailRequest request) {
        String fromAddress = request.getFrom() != null ? request.getFrom() : defaultFromEmail;

        try {
            CreateEmailOptions.Builder optionsBuilder = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(request.getTo())
                    .subject(request.getSubject());

            if (request.getHtmlContent() != null) {
                optionsBuilder.html(request.getHtmlContent());
            } else if (request.getTextContent() != null) {
                optionsBuilder.text(request.getTextContent());
            }

            if (request.getCc() != null && !request.getCc().isEmpty()) {
                optionsBuilder.cc(request.getCc());
            }

            if (request.getReplyTo() != null) {
                optionsBuilder.replyTo(request.getReplyTo());
            }

            CreateEmailResponse response = resend.emails().send(optionsBuilder.build());
            logger.info("Email sent successfully to {}. ID: {}", request.getTo(), response.getId());

        } catch (ResendException e) {
            logger.error("Resend API failed to send email to {}: {}", request.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send email via Resend: ", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending email to {}: {}", request.getTo(), e.getMessage());
            throw new RuntimeException("Unexpected error during email dispatch: ", e);
        }
    }
}
