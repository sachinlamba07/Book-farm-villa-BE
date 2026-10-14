package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, String>, JpaSpecificationExecutor<Property> {
    Optional<Property> findBySlug(String slug);
}
