package com.bookfarmvilla.util;

import com.bookfarmvilla.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListSortTest {

    private static final Set<String> ALLOWED_FIELDS = Set.of("name", "createdAt");

    @Test
    void keepsExistingPageableWhenSortByIsOmitted() {
        Pageable pageable = PageRequest.of(2, 15, Sort.by("createdAt").descending());

        Pageable result = ListSort.apply(pageable, null, null, ALLOWED_FIELDS);
        Pageable directionOnlyResult = ListSort.apply(pageable, null, "desc", ALLOWED_FIELDS);

        assertEquals(pageable, result);
        assertEquals(pageable, directionOnlyResult);
    }

    @Test
    void appliesNullLastSortAndStableIdTieBreaker() {
        Pageable pageable = PageRequest.of(2, 15);

        Pageable result = ListSort.apply(pageable, "name", "DESC", ALLOWED_FIELDS);

        assertEquals(2, result.getPageNumber());
        assertEquals(15, result.getPageSize());
        assertEquals(Sort.Direction.DESC, result.getSort().getOrderFor("name").getDirection());
        assertEquals(Sort.NullHandling.NULLS_LAST, result.getSort().getOrderFor("name").getNullHandling());
        assertEquals(Sort.Direction.ASC, result.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void defaultsSortDirectionToAscending() {
        Pageable result = ListSort.apply(PageRequest.of(0, 20), "createdAt", null, ALLOWED_FIELDS);

        assertEquals(Sort.Direction.ASC, result.getSort().getOrderFor("createdAt").getDirection());
    }

    @Test
    void rejectsUnknownSortField() {
        assertThrows(BadRequestException.class,
                () -> ListSort.apply(PageRequest.of(0, 20), "email", null, ALLOWED_FIELDS));
    }

    @Test
    void rejectsUnknownSortDirectionEvenWithoutSortField() {
        assertThrows(BadRequestException.class,
                () -> ListSort.apply(PageRequest.of(0, 20), null, "sideways", ALLOWED_FIELDS));
    }
}
