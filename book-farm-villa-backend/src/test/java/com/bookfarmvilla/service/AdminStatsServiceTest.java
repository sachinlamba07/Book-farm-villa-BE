package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.response.AdminStatsResponse;
import com.bookfarmvilla.dto.response.PropertyTypeCountRow;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.repository.BookingRepository;
import com.bookfarmvilla.repository.LeadRepository;
import com.bookfarmvilla.repository.PartnerRepository;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.VendorRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminStatsServiceTest {

    @Test
    void countsHolidayHomesAndLegacyAirbnbsTogether() {
        PropertyRepository propertyRepository = mock(PropertyRepository.class);
        VendorRepository vendorRepository = mock(VendorRepository.class);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        LeadRepository leadRepository = mock(LeadRepository.class);
        PartnerRepository partnerRepository = mock(PartnerRepository.class);
        when(propertyRepository.countGroupedByType()).thenReturn(List.of(
                new PropertyTypeCountRow(PropertyType.HOLIDAY_HOME, 3L),
                new PropertyTypeCountRow(PropertyType.AIRBNB, 2L)
        ));

        AdminStatsResponse result = new AdminStatsService(
                propertyRepository,
                vendorRepository,
                bookingRepository,
                leadRepository,
                partnerRepository
        ).getStats();

        assertEquals(5L, result.holidayHomes());
        assertEquals(5L, result.airbnbs());
        assertEquals(5L, result.totalListings());
    }
}
