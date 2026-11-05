package com.bookfarmvilla.service;

import com.bookfarmvilla.entity.Lead;
import com.bookfarmvilla.entity.Partner;
import com.bookfarmvilla.entity.Referral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 * Notifies the internal team when a new public submission comes in.
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

    @Async("notificationExecutor")
    public void notifyNewLead(Lead lead) {
        String target = lead.getProperty() != null
                ? "property '" + lead.getProperty().getTitle() + "'"
                : "vendor '" + lead.getVendor().getName() + "'";

        log.info("New lead notification queued for {}", target);
        sendEmail("New BookFarmVilla enquiry", buildLeadEmailBody(lead, target), "enquiry");
    }

    @Async("notificationExecutor")
    public void notifyNewPartner(Partner partner) {
        log.info("New partner request notification queued for business '{}'", partner.getBusinessName());
        sendEmail("New BookFarmVilla partner request", buildPartnerEmailBody(partner), "partner request");
    }

    @Async("notificationExecutor")
    public void notifyNewReferral(Referral referral) {
        log.info("New referral notification queued for {}", referral.getReferralNumber());
        sendEmail("New BookFarmVilla referral", buildReferralEmailBody(referral), "referral");
    }

    private void sendEmail(String subject, String text, String notificationType) {
        if (isBlank(resendApiKey) || isBlank(emailFrom) || isBlank(emailTo)) {
            log.warn("Resend email notification is not configured; {} notification was not sent",
                    notificationType);
            return;
        }

        try {
            restClient.post()
                    .uri(RESEND_EMAILS_URL)
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("User-Agent", "bookfarmvilla-backend/1.0")
                    .body(new ResendEmailRequest(
                            emailFrom,
                            List.of(emailTo),
                            subject,
                            text
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("{} email notification sent to {}", notificationType, emailTo);
        } catch (RestClientResponseException exception) {
            log.error("Resend rejected {} email (HTTP {})",
                    notificationType, exception.getStatusCode().value());
        } catch (RestClientException exception) {
            log.error("Could not send {} email through Resend ({})",
                    notificationType, exception.getClass().getSimpleName());
        }
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

    private String buildPartnerEmailBody(Partner partner) {
        return """
                A new partner request was submitted on BookFarmVilla.

                Business name: %s
                Business type: %s
                Contact name: %s
                City: %s
                Address: %s
                Phone: %s
                """.formatted(
                valueOrNotProvided(partner.getBusinessName()),
                valueOrNotProvided(partner.getBusinessType()),
                valueOrNotProvided(partner.getFullName()),
                valueOrNotProvided(partner.getCity()),
                valueOrNotProvided(partner.getLocation()),
                valueOrNotProvided(partner.getPhoneNumber())
        );
    }

    private String buildReferralEmailBody(Referral referral) {
        return """
                A new referral was submitted on BookFarmVilla.

                Referral number: %s
                Referrer name: %s
                Referrer phone: %s
                Referrer email: %s
                Referred friend name: %s
                Referred friend phone: %s
                Referred friend email: %s
                Property type: %s
                Event date: %s
                Message: %s
                """.formatted(
                valueOrNotProvided(referral.getReferralNumber()),
                valueOrNotProvided(referral.getReferrerName()),
                valueOrNotProvided(referral.getReferrerPhone()),
                valueOrNotProvided(referral.getReferrerEmail()),
                valueOrNotProvided(referral.getRefereeName()),
                valueOrNotProvided(referral.getRefereePhone()),
                valueOrNotProvided(referral.getRefereeEmail()),
                valueOrNotProvided(referral.getPropertyType()),
                valueOrNotProvided(referral.getEventDate()),
                valueOrNotProvided(referral.getMessage())
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
