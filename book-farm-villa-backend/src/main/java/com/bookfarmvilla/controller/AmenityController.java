package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.AmenityRequest;
import com.bookfarmvilla.dto.response.AmenityResponse;
import com.bookfarmvilla.service.AmenityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    private final AmenityService amenityService;

    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @GetMapping
    public ResponseEntity<List<AmenityResponse>> findAll() {
        return ResponseEntity.ok(amenityService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<AmenityResponse> create(@Valid @RequestBody AmenityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(amenityService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        amenityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
