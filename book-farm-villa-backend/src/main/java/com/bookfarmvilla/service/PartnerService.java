package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.PartnerRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PartnerResponse;
import com.bookfarmvilla.entity.Partner;
import com.bookfarmvilla.entity.enums.PartnerStatus;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.PartnerRepository;
import com.bookfarmvilla.util.SearchPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.notifyNewPartner(saved);
                }
            });
        } else {
            notificationService.notifyNewPartner(saved);
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<PartnerResponse> findAll(PartnerStatus status, String keyword, Pageable pageable) {
        String pattern = SearchPatterns.contains(keyword);
        Page<Partner> page;
        if (pattern != null) {
            page = partnerRepository.search(status, pattern, pageable);
        } else if (status != null) {
            page = partnerRepository.findByStatus(status, pageable);
        } else {
            page = partnerRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public PartnerResponse updateStatus(String id, PartnerStatus status) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Partner", id));
        partner.setStatus(status);
        Partner updated = partnerRepository.save(partner);
        log.info("Updated partner request id={} status={}", id, status);
        return toResponse(updated);
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
                p.getCreatedAt(), p.getModifiedAt());
    }
}