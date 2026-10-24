package com.bookfarmvilla.dto.response;

import java.util.List;

public record ReferralStrategySectionData(
        String title,
        String description,
        List<StrategyStep> steps
) {
    public record StrategyStep(
            String stepNumber,
            String title,
            String description
    ) {}
}