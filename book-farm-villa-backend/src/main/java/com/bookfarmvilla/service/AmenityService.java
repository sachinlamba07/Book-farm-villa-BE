package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.AmenityRequest;
import com.bookfarmvilla.dto.response.AmenityResponse;
import com.bookfarmvilla.entity.Amenity;
import com.bookfarmvilla.exception.DuplicateResourceException;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.AmenityRepository;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AmenityService {

    private final AmenityRepository amenityRepository;

    public AmenityService(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @Cacheable(cacheNames = "amenities", key = "'all'")
    public List<AmenityResponse> findAll() {
        return amenityRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "amenities", allEntries = true)
    public AmenityResponse create(AmenityRequest request) {
        if (amenityRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("An amenity named '" + request.name() + "' already exists");
        }
        Amenity amenity = new Amenity();
        amenity.setId(request.id()); // preserved if provided, generated in BaseEntity otherwise
        amenity.setName(request.name());
        return toResponse(amenityRepository.save(amenity));
    }

    @Transactional
    @CacheEvict(cacheNames = "amenities", allEntries = true)
    public void delete(String id) {
        if (!amenityRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Amenity", id);
        }
        amenityRepository.deleteById(id);
    }

    private AmenityResponse toResponse(Amenity amenity) {
        return new AmenityResponse(amenity.getId(), amenity.getName());
    }
}
