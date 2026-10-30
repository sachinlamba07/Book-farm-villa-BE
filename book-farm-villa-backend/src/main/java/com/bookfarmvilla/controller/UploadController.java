package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.response.UploadSignatureResponse;
import com.bookfarmvilla.service.CloudinaryUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/uploads")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class UploadController {

    private final CloudinaryUploadService uploadService;

    public UploadController(CloudinaryUploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * Returns a signature valid for one upload of the given type (image or video).
     */
    @PostMapping("/signature")
    public ResponseEntity<UploadSignatureResponse> signature(@RequestParam String resourceType) {
        return ResponseEntity.ok(uploadService.sign(resourceType));
    }
}