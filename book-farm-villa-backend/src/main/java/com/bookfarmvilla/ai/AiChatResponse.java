package com.bookfarmvilla.ai;

import com.bookfarmvilla.dto.response.PropertyResponse;
import com.bookfarmvilla.dto.response.VendorResponse;

import java.util.List;

public record AiChatResponse(
        String intent,
        String message,
        List<PropertyResponse> properties,
        List<VendorResponse> vendors,
        AiRequirements requirements,
        boolean locationSupported,
        String locationMessage
) {
}
