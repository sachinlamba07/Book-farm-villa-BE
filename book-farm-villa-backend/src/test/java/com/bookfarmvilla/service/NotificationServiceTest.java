package com.bookfarmvilla.service;

import com.bookfarmvilla.entity.Lead;
import com.bookfarmvilla.entity.Property;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NotificationServiceTest {

    @Test
    void sendsNewLeadEmailThroughResendApi() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        NotificationService service = new NotificationService(
                restClientBuilder,
                "info@bookfarmvilla.com",
                "BookFarmVilla <verified@bookfarmvilla.com>",
                "local-test-token"
        );

        Property property = new Property();
        property.setTitle("Green Valley Villa");
        Lead lead = new Lead();
        lead.setProperty(property);
        lead.setName("Asha");
        lead.setPhone("+911234567890");
        lead.setEmail("asha@example.com");
        lead.setMessage("Please call me back.");

        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer local-test-token"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "from": "BookFarmVilla <verified@bookfarmvilla.com>",
                          "to": ["info@bookfarmvilla.com"],
                          "subject": "New BookFarmVilla enquiry",
                          "text": "A new enquiry was submitted on BookFarmVilla.\\n\\nEnquiry for: property 'Green Valley Villa'\\nName: Asha\\nPhone: +911234567890\\nEmail: asha@example.com\\nPreferred date: Not provided\\nEvent type: Not provided\\nMessage: Please call me back.\\n"
                        }
                        """))
                .andRespond(withSuccess());

        service.notifyNewLead(lead);

        server.verify();
    }
}
