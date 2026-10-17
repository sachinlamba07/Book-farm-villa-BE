package com.bookfarmvilla.service;

import com.bookfarmvilla.entity.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Notifies the internal team when a new lead comes in.
 *
 * Sends enquiry email when SMTP is configured; otherwise logs that delivery
 * is not configured without failing the enquiry request.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String enquiryEmail;
    private final String fromEmail;
    private final String smtpUsername;
    private final String smtpPassword;

    public NotificationService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.notifications.enquiry-email:}") String enquiryEmail,
            @Value("${app.notifications.from-email:}") String fromEmail,
            @Value("${spring.mail.username:}") String smtpUsername,
            @Value("${spring.mail.password:}") String smtpPassword
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.enquiryEmail = enquiryEmail;
        this.fromEmail = fromEmail;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    public void notifyNewLead(Lead lead) {
        String target = lead.getProperty() != null
                ? "property '" + lead.getProperty().getTitle() + "'"
                : "vendor '" + lead.getVendor().getName() + "'";

        log.info("New lead received for {} — name: {}, phone: {}, preferredDate: {}",
                target, lead.getName(), lead.getPhone(), lead.getPreferredDate());

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || isBlank(enquiryEmail) || isBlank(fromEmail)
                || isBlank(smtpUsername) || isBlank(smtpPassword)) {
            log.warn("Enquiry email notification is not configured; lead notification was not sent");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(enquiryEmail);
        message.setSubject("New BookFarmVilla enquiry");
        message.setText(buildLeadEmailBody(lead, target));

        try {
            mailSender.send(message);
            log.info("Enquiry email notification sent to {}", enquiryEmail);
        } catch (MailException exception) {
            log.error("Failed to send enquiry email notification to {} ({})",
                    enquiryEmail, exception.getClass().getSimpleName());
        }
    }

    public void notifyNewPartner(com.bookfarmvilla.entity.Partner partner) {
        log.info("New partner request received — business: '{}', type: {}, city: {}, contact: {}",
                partner.getBusinessName(), partner.getBusinessType(), partner.getCity(), partner.getPhoneNumber());

        // Partner notifications can use the same email integration when required.
    }

    private String buildLeadEmailBody(Lead lead, String target) {
        return """
                A new enquiry was submitted on BookFarmVilla.

                Enquiry for: %s
                Name: %s
                Phone: %s
                Email: %s
                Preferred date: %s
                Event type: %s
                Message: %s
                """.formatted(
                target,
                valueOrNotProvided(lead.getName()),
                valueOrNotProvided(lead.getPhone()),
                valueOrNotProvided(lead.getEmail()),
                valueOrNotProvided(lead.getPreferredDate()),
                valueOrNotProvided(lead.getEventType()),
                valueOrNotProvided(lead.getMessage())
        );
    }

    private String valueOrNotProvided(Object value) {
        return value == null || value.toString().isBlank() ? "Not provided" : value.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
