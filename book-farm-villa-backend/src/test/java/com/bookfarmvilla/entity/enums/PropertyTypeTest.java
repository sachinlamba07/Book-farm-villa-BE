package com.bookfarmvilla.entity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyTypeTest {

    @Test
    @SuppressWarnings("deprecation")
    void legacyAirbnbValueUsesHolidayHomeType() {
        assertEquals(PropertyType.HOLIDAY_HOME, PropertyType.AIRBNB.canonical());
    }

    @Test
    void canonicalHolidayHomeValueRemainsUnchanged() {
        assertEquals(PropertyType.HOLIDAY_HOME, PropertyType.HOLIDAY_HOME.canonical());
    }
}
