package com.bookfarmvilla.dto.response;

public record UploadSignatureResponse(
        String cloudName,
        String apiKey,
        long timestamp,
        String signature,
        String folder,
        String allowedFormats,
        String resourceType) {
}