package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.PartnerRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PartnerResponse;
import com.bookfarmvilla.entity.Partner;
import com.bookfarmvilla.entity.enums.PartnerStatus;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.PartnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartnerService {

    private static final Logger log = LoggerFactory.getLogger(PartnerService.class);

    private final PartnerRepository partnerRepository;
    private final NotificationService notificationService;

    public PartnerService(PartnerRepository partnerRepository, NotificationService notificationService) {
        this.partnerRepository = partnerRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public PartnerResponse create(PartnerRequest request) {
        Partner partner = new Partner();
        partner.setId(request.id()); // preserved if provided, generated in BaseEntity otherwise
        partner.setBusinessName(request.businessName());
        partner.setFullName(request.fullName());
        partner.setBusinessType(request.businessType());
        partner.setCity(request.city());
        partner.setLocation(request.location());
        partner.setPhoneNumber(request.phoneNumber());
        partner.setStatus(PartnerStatus.PENDING);

        Partner saved = partnerRepository.save(partner);
        log.info("Created partner request id={}, business='{}'", saved.getId(), saved.getBusinessName());
        notificationService.notifyNewPartner(saved);
        return toResponse(saved);
    }

    public PageResponse<PartnerResponse> findAll(PartnerStatus status, Pageable pageable) {
        Page<Partner> page = status != null
                ? partnerRepository.findByStatus(status, pageable)
                : partnerRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public void delete(String id) {
        if (!partnerRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Partner", id);
        }
        partnerRepository.deleteById(id);
        log.info("Deleted partner request id={}", id);
    }

    private PartnerResponse toResponse(Partner p) {
        return new PartnerResponse(
                p.getId(), p.getBusinessName(), p.getFullName(), p.getBusinessType(),
                p.getCity(), p.getLocation(), p.getPhoneNumber(), p.getStatus(),
                p.getCreatedAt(), p.getModifiedAt()
        );
    }
}
