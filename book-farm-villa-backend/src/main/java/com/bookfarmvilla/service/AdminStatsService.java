package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.response.AdminStatsResponse;
import com.bookfarmvilla.dto.response.PropertyTypeCountRow;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.repository.BookingRepository;
import com.bookfarmvilla.repository.LeadRepository;
import com.bookfarmvilla.repository.PartnerRepository;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service
public class AdminStatsService {

    private final PropertyRepository propertyRepository;
    private final VendorRepository vendorRepository;
    private final BookingRepository bookingRepository;
    private final LeadRepository leadRepository;
    private final PartnerRepository partnerRepository;

    public AdminStatsService(
            PropertyRepository propertyRepository,
            VendorRepository vendorRepository,
            BookingRepository bookingRepository,
            LeadRepository leadRepository,
            PartnerRepository partnerRepository) {
        this.propertyRepository = propertyRepository;
        this.vendorRepository = vendorRepository;
        this.bookingRepository = bookingRepository;
        this.leadRepository = leadRepository;
        this.partnerRepository = partnerRepository;
    }

    /** Not cached: admins expect the dashboard to reflect changes immediately. */
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        Map<PropertyType, Long> byType = new EnumMap<>(PropertyType.class);
        for (PropertyTypeCountRow row : propertyRepository.countGroupedByType()) {
            byType.put(row.type(), row.count());
        }
        long totalListings = byType.values().stream().mapToLong(Long::longValue).sum();
        long holidayHomes = byType.getOrDefault(PropertyType.HOLIDAY_HOME, 0L)
                + byType.getOrDefault(PropertyType.AIRBNB, 0L);

        return new AdminStatsResponse(
                totalListings,
                byType.getOrDefault(PropertyType.FARMHOUSE, 0L),
                byType.getOrDefault(PropertyType.VILLA, 0L),
                byType.getOrDefault(PropertyType.WEDDING_LAWN, 0L),
                holidayHomes,
                holidayHomes,
                vendorRepository.count(),
                bookingRepository.count(),
                leadRepository.count(),
                partnerRepository.count());
    }
}