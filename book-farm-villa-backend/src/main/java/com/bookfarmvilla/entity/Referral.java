package com.bookfarmvilla.entity;

import com.bookfarmvilla.entity.enums.ReferralStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "referrals")
@Getter
@Setter
public class Referral extends BaseEntity {

    @Column(name = "referral_number", nullable = false, unique = true, length = 30)
    private String referralNumber;

    @Column(name = "customer_mobile", nullable = false, length = 30)
    private String customerMobile;

    @Column(name = "referrer_mobile", length = 30)
    private String referrerMobile;

    @Column(name = "event", length = 100)
    private String event;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "property_id", length = 36)
    private String propertyId;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReferralStatus status;

    @Column(name = "booking_amount")
    private Double bookingAmount;

    @Column(name = "reward_amount")
    private Double rewardAmount;
}