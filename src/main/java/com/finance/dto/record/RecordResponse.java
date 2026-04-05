package com.finance.dto.record;

import com.finance.enums.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecordResponse(
        Long id,
        BigDecimal amount,
        RecordType type,
        String category,
        LocalDate date,
        String notes,
        Long userId,
        String userName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
