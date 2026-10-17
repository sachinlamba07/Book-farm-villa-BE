package com.bookfarmvilla.service;

import com.bookfarmvilla.entity.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 * Notifies the internal team when a new lead comes in.
 *
 * Sends enquiry email through Resend's HTTPS API, which is available from
 * Render Free services unlike outbound SMTP.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String RESEND_EMAILS_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final String emailTo;
    private final String emailFrom;
    private final String resendApiKey;

    public NotificationService(
            RestClient.Builder restClientBuilder,
            @Value("${app.notifications.email-to:}") String emailTo,
            @Value("${app.notifications.email-from:}") String emailFrom,
            @Value("${app.notifications.resend-api-key:}") String resendApiKey
    ) {
        this.restClient = restClientBuilder.build();
        this.emailTo = emailTo;
        this.emailFrom = emailFrom;
        this.resendApiKey = resendApiKey;
    }

    public void notifyNewLead(Lead lead) {
        String target = lead.getProperty() != null
                ? "property '" + lead.getProperty().getTitle() + "'"
                : "vendor '" + lead.getVendor().getName() + "'";

        log.info("New lead received for {} — name: {}, phone: {}, preferredDate: {}",
                target, lead.getName(), lead.getPhone(), lead.getPreferredDate());

        if (isBlank(resendApiKey) || isBlank(emailFrom) || isBlank(emailTo)) {
            log.warn("Resend email notification is not configured; lead notification was not sent");
            return;
        }

        try {
            restClient.post()
                    .uri(RESEND_EMAILS_URL)
                    .header("Authorization", "Bearer " + resendApiKey)
                    .body(new ResendEmailRequest(
                            emailFrom,
                            List.of(emailTo),
                            "New BookFarmVilla enquiry",
                            buildLeadEmailBody(lead, target)
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Enquiry email notification sent to {}", emailTo);
        } catch (RestClientResponseException exception) {
            log.error("Resend rejected enquiry email (HTTP {})", exception.getStatusCode().value());
        } catch (RestClientException exception) {
            log.error("Could not send enquiry email through Resend ({})",
                    exception.getClass().getSimpleName());
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

    private record ResendEmailRequest(
            String from,
            List<String> to,
            String subject,
            String text
    ) {}
}
