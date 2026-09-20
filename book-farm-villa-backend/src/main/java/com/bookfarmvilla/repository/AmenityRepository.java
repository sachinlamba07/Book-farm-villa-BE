package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmenityRepository extends JpaRepository<Amenity, String> {
    Optional<Amenity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
