package com.bookfarmvilla.service;

import com.bookfarmvilla.entity.Lead;
import com.bookfarmvilla.entity.Partner;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.Referral;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
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
                .andExpect(header("User-Agent", "bookfarmvilla-backend/1.0"))
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

    @Test
    void sendsNewPartnerEmailThroughResendApi() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        NotificationService service = new NotificationService(
                restClientBuilder,
                "info@bookfarmvilla.com",
                "BookFarmVilla <verified@bookfarmvilla.com>",
                "local-test-token"
        );

        Partner partner = new Partner();
        partner.setBusinessName("Green Valley");
        partner.setBusinessType("Holiday Homes");
        partner.setFullName("Asha Sharma");
        partner.setCity("Noida");
        partner.setLocation("Sector 18, Noida");
        partner.setPhoneNumber("+911234567890");

        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "from": "BookFarmVilla <verified@bookfarmvilla.com>",
                          "to": ["info@bookfarmvilla.com"],
                          "subject": "New BookFarmVilla partner request",
                          "text": "A new partner request was submitted on BookFarmVilla.\\n\\nBusiness name: Green Valley\\nBusiness type: Holiday Homes\\nContact name: Asha Sharma\\nCity: Noida\\nAddress: Sector 18, Noida\\nPhone: +911234567890\\n"
                        }
                        """))
                .andRespond(withSuccess());

        service.notifyNewPartner(partner);

        server.verify();
    }

    @Test
    void sendsNewReferralEmailThroughResendApi() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        NotificationService service = new NotificationService(
                restClientBuilder,
                "info@bookfarmvilla.com",
                "BookFarmVilla <verified@bookfarmvilla.com>",
                "local-test-token"
        );

        Referral referral = new Referral();
        referral.setReferralNumber("BFV-REF-2026-000001");
        referral.setReferrerName("Asha");
        referral.setReferrerPhone("+911234567890");
        referral.setReferrerEmail("asha@example.com");
        referral.setRefereeName("Riya");
        referral.setRefereePhone("+919876543210");
        referral.setRefereeEmail("riya@example.com");
        referral.setPropertyType("HOLIDAY_HOME");
        referral.setMessage("Please contact Riya.");

        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "from": "BookFarmVilla <verified@bookfarmvilla.com>",
                          "to": ["info@bookfarmvilla.com"],
                          "subject": "New BookFarmVilla referral",
                          "text": "A new referral was submitted on BookFarmVilla.\\n\\nReferral number: BFV-REF-2026-000001\\nReferrer name: Asha\\nReferrer phone: +911234567890\\nReferrer email: asha@example.com\\nReferred friend name: Riya\\nReferred friend phone: +919876543210\\nReferred friend email: riya@example.com\\nProperty type: HOLIDAY_HOME\\nEvent date: Not provided\\nMessage: Please contact Riya.\\n"
                        }
                        """))
                .andRespond(withSuccess());

        service.notifyNewReferral(referral);

        server.verify();
    }
}
