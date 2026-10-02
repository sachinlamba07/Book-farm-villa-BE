package com.bookfarmvilla.service;

import com.bookfarmvilla.entity.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Notifies the internal team when a new lead comes in.
 *
 * This is intentionally a simple logging implementation. To wire up real
 * notifications, inject an email client (Resend/SendGrid) and/or SMS client
 * (Twilio) here and call them from notifyNewLead - no other code needs to change.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void notifyNewLead(Lead lead) {
        String target = lead.getProperty() != null
                ? "property '" + lead.getProperty().getTitle() + "'"
                : "vendor '" + lead.getVendor().getName() + "'";

        log.info("New lead received for {} — name: {}, phone: {}, preferredDate: {}",
                target, lead.getName(), lead.getPhone(), lead.getPreferredDate());

        // TODO: plug in email (Resend/SendGrid) and/or SMS/WhatsApp (Twilio) here.
        // e.g. emailClient.send(teamInbox, "New lead: " + target, buildLeadEmailBody(lead));
    }

    public void notifyNewPartner(com.bookfarmvilla.entity.Partner partner) {
        log.info("New partner request received — business: '{}', type: {}, city: {}, contact: {}",
                partner.getBusinessName(), partner.getBusinessType(), partner.getCity(), partner.getPhoneNumber());

        // TODO: plug in email (Resend/SendGrid) and/or SMS/WhatsApp (Twilio) here, same as notifyNewLead.
    }
}
