package com.capstone.scheduler.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmailRequest {
    // Required fields (usually validated at the controller/service level)
    private List<String> to;
    private String subject;

    // Content (Provide either HTML or Text)
    private String htmlContent;
    private String textContent;

    // Optional fields
    private String from; // Allows overriding the default "noreply" address
    private String replyTo; // Allows setting the Reply-To header
    private List<String> cc;
    private List<String> bcc;

    // You can easily add attachments here later if needed:
    // private List<Attachment> attachments;
}
