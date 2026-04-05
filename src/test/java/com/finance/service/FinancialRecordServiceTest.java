package com.finance.service;

import com.finance.dto.record.RecordRequest;
import com.finance.dto.record.RecordResponse;
import com.finance.dto.record.RecordFilterParams;
import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.enums.RecordType;
import com.finance.exception.AppException;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FinancialRecordService recordService;

    private User demoUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        demoUser = new User();
        demoUser.setId(1L);
        demoUser.setEmail("admin@finance.dev");
        demoUser.setName("Admin User");
    }

    @Test
    void testCreateRecord() {
        RecordRequest req = new RecordRequest(new BigDecimal("1000.0"), RecordType.INCOME, "Salary", LocalDate.now(), "Monthly salary");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(demoUser));
        FinancialRecord saved = new FinancialRecord();
        saved.setId(10L);
        saved.setUser(demoUser);
        saved.setAmount(req.amount());
        saved.setType(req.type());
        saved.setCategory(req.category());
        saved.setDate(req.date());
        saved.setNotes(req.notes());
        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(saved);
        RecordResponse resp = recordService.createRecord(req, "admin@finance.dev");
        assertEquals(10L, resp.id());
        assertEquals(new BigDecimal("1000.0"), resp.amount());
        assertEquals("Salary", resp.category());
    }

    @Test
    void testGetRecordsWithFilter() {
        RecordFilterParams filters = new RecordFilterParams(null, "salary", null, null, 0, 10, "createdAt", "DESC");
        FinancialRecord rec = new FinancialRecord();
        rec.setId(20L);
        rec.setUser(demoUser);
        rec.setAmount(new BigDecimal("2000.0"));
        rec.setType(RecordType.INCOME);
        rec.setCategory("Salary");
        rec.setDate(LocalDate.now());
        rec.setNotes("Bonus");
        Page<FinancialRecord> page = new PageImpl<>(List.of(rec));
        when(recordRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        Page<RecordResponse> result = recordService.getRecords(filters);
        assertEquals(1, result.getTotalElements());
        RecordResponse rr = result.getContent().get(0);
        assertEquals("Salary", rr.category());
    }

    @Test
    void testSoftDeleteRecord() {
        FinancialRecord existing = new FinancialRecord();
        existing.setId(30L);
        existing.setUser(demoUser);
        when(recordRepository.findById(30L)).thenReturn(Optional.of(existing));
        recordService.softDeleteRecord(30L);
        verify(recordRepository).save(argThat(r -> r.getDeletedAt() != null));
    }

    @Test
    void testUpdateRecord() {
        FinancialRecord existing = new FinancialRecord();
        existing.setId(40L);
        existing.setUser(demoUser);
        when(recordRepository.findById(40L)).thenReturn(Optional.of(existing));
        RecordRequest updateReq = new RecordRequest(new BigDecimal("1500.0"), RecordType.EXPENSE, "Office", LocalDate.now(), "Stationery");
        when(recordRepository.save(any(FinancialRecord.class))).thenAnswer(i -> i.getArgument(0));
        RecordResponse resp = recordService.updateRecord(40L, updateReq);
        assertEquals(new BigDecimal("1500.0"), resp.amount());
        assertEquals(RecordType.EXPENSE, resp.type());
        assertEquals("Office", resp.category());
    }

    @Test
    void testGetRecordByIdNotFound() {
        when(recordRepository.findById(99L)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> recordService.getRecordById(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
