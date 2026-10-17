package com.bookfarmvilla.service;

import com.bookfarmvilla.entity.Lead;
import com.bookfarmvilla.entity.Property;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Test
    void sendsNewLeadEmailToConfiguredInbox() {
        JavaMailSender sender = mock(JavaMailSender.class);

        ObjectProvider<JavaMailSender> senderProvider = mock(ObjectProvider.class);
        when(senderProvider.getIfAvailable()).thenReturn(sender);

        NotificationService service = new NotificationService(
                senderProvider,
                "info@bookfarmvilla.com",
                "info@bookfarmvilla.com",
                "info@bookfarmvilla.com",
                "test-smtp-password"
        );

        Property property = mock(Property.class);
        when(property.getTitle()).thenReturn("Green Valley Villa");
        Lead lead = new Lead();
        lead.setProperty(property);
        lead.setName("Asha");
        lead.setPhone("+911234567890");
        lead.setEmail("asha@example.com");
        lead.setMessage("Please call me back.");

        service.notifyNewLead(lead);

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("info@bookfarmvilla.com", message.getTo()[0]);
        assertEquals("info@bookfarmvilla.com", message.getFrom());
        assertEquals("New BookFarmVilla enquiry", message.getSubject());
        assertTrue(message.getText().contains("Green Valley Villa"));
        assertTrue(message.getText().contains("Asha"));
        assertTrue(message.getText().contains("Please call me back."));
    }
}
