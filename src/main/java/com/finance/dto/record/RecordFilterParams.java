package com.finance.dto.record;

import com.finance.enums.RecordType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record RecordFilterParams(
        RecordType type,
        String category,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        int page,
        int size,
        String sortBy,
        String direction
) {
    public RecordFilterParams {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.isBlank()) sortBy = "date";
        if (direction == null || direction.isBlank()) direction = "DESC";
    }
}
