package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.LeadRequest;
import com.bookfarmvilla.dto.response.LeadResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.Lead;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.entity.enums.LeadStatus;
import com.bookfarmvilla.exception.BadRequestException;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.LeadRepository;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.VendorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final PropertyRepository propertyRepository;
    private final VendorRepository vendorRepository;
    private final NotificationService notificationService;

    public LeadService(
            LeadRepository leadRepository,
            PropertyRepository propertyRepository,
            VendorRepository vendorRepository,
            NotificationService notificationService
    ) {
        this.leadRepository = leadRepository;
        this.propertyRepository = propertyRepository;
        this.vendorRepository = vendorRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public LeadResponse create(LeadRequest request) {
        boolean hasProperty = request.propertyId() != null && !request.propertyId().isBlank();
        boolean hasVendor = request.vendorId() != null && !request.vendorId().isBlank();

        if (hasProperty == hasVendor) {
            // both blank, or both set - either way that's invalid
            throw new BadRequestException("A lead must reference exactly one property or one vendor, not both or neither");
        }

        Lead lead = new Lead();
        lead.setId(request.id());
        lead.setName(request.name());
        lead.setPhone(request.phone());
        lead.setEmail(request.email());
        lead.setPreferredDate(request.preferredDate());
        lead.setEventType(request.eventType());
        lead.setMessage(request.message());
        lead.setStatus(LeadStatus.NEW);

        if (hasProperty) {
            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Property", request.propertyId()));
            lead.setProperty(property);
        } else {
            Vendor vendor = vendorRepository.findById(request.vendorId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Vendor", request.vendorId()));
            lead.setVendor(vendor);
        }

        Lead saved = leadRepository.save(lead);
        notificationService.notifyNewLead(saved);
        return toResponse(saved);
    }

    public PageResponse<LeadResponse> findAll(LeadStatus status, Pageable pageable) {
        Page<Lead> page = status != null
                ? leadRepository.findByStatus(status, pageable)
                : leadRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public LeadResponse updateStatus(String id, LeadStatus status) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Lead", id));
        lead.setStatus(status);
        return toResponse(leadRepository.save(lead));
    }

    private LeadResponse toResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getProperty() != null ? lead.getProperty().getId() : null,
                lead.getProperty() != null ? lead.getProperty().getTitle() : null,
                lead.getProperty() != null ? lead.getProperty().getOriginalPropertyName() : null,
                lead.getVendor() != null ? lead.getVendor().getId() : null,
                lead.getVendor() != null ? lead.getVendor().getName() : null,
                lead.getName(),
                lead.getPhone(),
                lead.getEmail(),
                lead.getPreferredDate(),
                lead.getEventType(),
                lead.getMessage(),
                lead.getStatus(),
                lead.getCreatedAt(),
                lead.getModifiedAt()
        );
    }
}
