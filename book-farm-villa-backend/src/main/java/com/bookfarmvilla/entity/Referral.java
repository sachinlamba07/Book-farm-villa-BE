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

    @Column(name = "referrer_name", nullable = false, length = 100)
    private String referrerName;

    @Column(name = "referrer_phone", nullable = false, length = 30)
    private String referrerPhone;

    @Column(name = "referrer_email", length = 100)
    private String referrerEmail;

    @Column(name = "referee_name", nullable = false, length = 100)
    private String refereeName;

    @Column(name = "referee_phone", nullable = false, length = 30)
    private String refereePhone;

    @Column(name = "referee_email", length = 100)
    private String refereeEmail;

    @Column(name = "property_type", length = 50)
    private String propertyType;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReferralStatus status;

    @Column(name = "booking_amount")
    private Double bookingAmount;

    @Column(name = "reward_amount")
    private Double rewardAmount;

    @Column(name = "property_id", length = 36)
    private String propertyId;
}