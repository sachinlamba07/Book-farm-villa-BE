package com.bookfarmvilla.entity.enums;

public enum PropertyType {
    FARMHOUSE,
    VILLA,
    WEDDING_LAWN,
    HOLIDAY_HOME,
    @Deprecated(since = "1.0", forRemoval = false)
    AIRBNB;

    public PropertyType canonical() {
        return this == AIRBNB ? HOLIDAY_HOME : this;
    }
}
