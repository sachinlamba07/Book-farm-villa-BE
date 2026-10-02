package com.bookfarmvilla.entity;

import com.bookfarmvilla.entity.enums.PartnerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A "Partner with us" business enquiry submitted from the public site
 * (farmhouse/villa/wedding-lawn owners, vendors, etc. wanting to list with us).
 */
@Entity
@Table(name = "partners")
@Getter
@Setter
@NoArgsConstructor
public class Partner extends BaseEntity {

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "business_type", nullable = false)
    private String businessType;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String location;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerStatus status = PartnerStatus.PENDING;
}
