package com.finance.service;

import com.finance.dto.record.RecordFilterParams;
import com.finance.dto.record.RecordRequest;
import com.finance.dto.record.RecordResponse;
import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.enums.RecordType;
import com.finance.exception.AppException;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    public FinancialRecordService(FinancialRecordRepository recordRepository,
                                  UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    public RecordResponse createRecord(RecordRequest req, String email) {
        User user = findUserByEmail(email);
        FinancialRecord record = FinancialRecord.builder()
                .amount(req.amount())
                .type(req.type())
                .category(req.category().trim())
                .date(req.date())
                .notes(req.notes())
                .user(user)
                .build();
        return toResponse(recordRepository.save(record));
    }

    public Page<RecordResponse> getRecords(RecordFilterParams filters) {
        Sort sort = filters.direction().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();
        Pageable pageable = PageRequest.of(filters.page(), filters.size(), sort);

        Specification<FinancialRecord> spec = buildSpec(filters);
        return recordRepository.findAll(spec, pageable).map(FinancialRecordService::toResponse);
    }

    public RecordResponse getRecordById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public RecordResponse updateRecord(Long id, RecordRequest req) {
        FinancialRecord record = findOrThrow(id);
        record.setAmount(req.amount());
        record.setType(req.type());
        record.setCategory(req.category().trim());
        record.setDate(req.date());
        record.setNotes(req.notes());
        return toResponse(recordRepository.save(record));
    }

    public void softDeleteRecord(Long id) {
        FinancialRecord record = findOrThrow(id);
        record.setDeletedAt(LocalDateTime.now());
        recordRepository.save(record);
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private Specification<FinancialRecord> buildSpec(RecordFilterParams f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (f.type() != null) {
                predicates.add(cb.equal(root.get("type"), f.type()));
            }
            if (f.category() != null && !f.category().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("category")),
                        "%" + f.category().toLowerCase() + "%"));
            }
            if (f.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), f.startDate()));
            }
            if (f.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), f.endDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private FinancialRecord findOrThrow(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Financial record not found with id: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public static RecordResponse toResponse(FinancialRecord r) {
        return new RecordResponse(
                r.getId(), r.getAmount(), r.getType(), r.getCategory(),
                r.getDate(), r.getNotes(),
                r.getUser().getId(), r.getUser().getName(),
                r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}
