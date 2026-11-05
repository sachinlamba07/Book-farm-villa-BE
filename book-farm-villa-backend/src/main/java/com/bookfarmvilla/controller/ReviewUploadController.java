package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.response.UploadSignatureResponse;
import com.bookfarmvilla.service.CloudinaryUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewUploadController {

    private final CloudinaryUploadService cloudinaryUploadService;

    public ReviewUploadController(CloudinaryUploadService cloudinaryUploadService) {
        this.cloudinaryUploadService = cloudinaryUploadService;
    }

    @PostMapping("/api/review-uploads/signature")
    public ResponseEntity<UploadSignatureResponse> createSignature(
            @RequestParam String resourceType
    ) {
        return ResponseEntity.ok(cloudinaryUploadService.signReviewMedia(resourceType));
    }
}
