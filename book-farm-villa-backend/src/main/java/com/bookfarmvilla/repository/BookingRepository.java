package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookingRepository extends JpaRepository<Booking, String>, JpaSpecificationExecutor<Booking> {
}
