package com.bookfarmvilla.dto.response;

import java.util.List;

public record ReferralPolicyData(
        String title,
        String lastUpdated,
        List<String> terms
) {
}