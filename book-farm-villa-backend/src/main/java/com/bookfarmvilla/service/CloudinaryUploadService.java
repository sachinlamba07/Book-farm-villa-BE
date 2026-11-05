package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.response.UploadSignatureResponse;
import com.bookfarmvilla.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Issues short-lived Cloudinary upload signatures.
 * The API secret never leaves the server; Cloudinary rejects signatures older
 * than 1 hour.
 */
@Service
public class CloudinaryUploadService {

    private static final Map<String, String> ALLOWED_FORMATS = Map.of(
            "image", "jpg,jpeg,png,webp",
            "video", "mp4,webm,mov");

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String baseFolder;

    public CloudinaryUploadService(
            @Value("${app.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.cloudinary.api-key:}") String apiKey,
            @Value("${app.cloudinary.api-secret:}") String apiSecret,
            @Value("${app.cloudinary.folder:bookfarmvilla}") String baseFolder) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.baseFolder = baseFolder;
    }

    public UploadSignatureResponse sign(String resourceType) {
        return sign(resourceType, baseFolder + "/" + normalizedResourceType(resourceType) + "s");
    }

    public UploadSignatureResponse signReviewMedia(String resourceType) {
        return sign(resourceType, baseFolder + "/review-media/" + normalizedResourceType(resourceType) + "s");
    }

    private UploadSignatureResponse sign(String resourceType, String folder) {
        String type = resourceType == null ? "" : resourceType.trim().toLowerCase(Locale.ROOT);
        String formats = ALLOWED_FORMATS.get(type);
        if (formats == null) {
            throw new BadRequestException("resourceType must be 'image' or 'video'");
        }
        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new BadRequestException("Media uploads are not configured on the server.");
        }
        long timestamp = Instant.now().getEpochSecond();

        // Cloudinary signs every upload parameter except file, api_key,
        // resource_type and cloud_name, sorted alphabetically.
        Map<String, String> params = new TreeMap<>();
        params.put("allowed_formats", formats);
        params.put("folder", folder);
        params.put("timestamp", String.valueOf(timestamp));

        String toSign = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return new UploadSignatureResponse(
                cloudName, apiKey, timestamp, sha1Hex(toSign + apiSecret), folder, formats, type);
    }

    private String normalizedResourceType(String resourceType) {
        String type = resourceType == null ? "" : resourceType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_FORMATS.containsKey(type)) {
            throw new BadRequestException("resourceType must be 'image' or 'video'");
        }
        return type;
    }

    private static String sha1Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-1 is not available", ex);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}