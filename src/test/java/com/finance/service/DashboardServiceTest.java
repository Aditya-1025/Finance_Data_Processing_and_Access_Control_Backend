package com.finance.service;

import com.finance.dto.record.RecordResponse;
import com.finance.enums.RecordType;
import com.finance.repository.FinancialRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DashboardServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSummary() {
        when(recordRepository.sumByType(RecordType.INCOME)).thenReturn(new BigDecimal("1000"));
        when(recordRepository.sumByType(RecordType.EXPENSE)).thenReturn(new BigDecimal("400"));
        Map<String, Object> summary = dashboardService.getSummary();
        assertEquals(new BigDecimal("1000"), summary.get("totalIncome"));
        assertEquals(new BigDecimal("400"), summary.get("totalExpenses"));
        assertEquals(new BigDecimal("600"), summary.get("netBalance"));
    }

    @Test
    void testGetCategoryBreakdown() {
        Object[] row = new Object[]{"Salary", RecordType.INCOME, new BigDecimal("2000")};
        List<Object[]> rows = new ArrayList<>();
        rows.add(row);
        when(recordRepository.categoryBreakdown()).thenReturn(rows);
        List<Map<String, Object>> result = dashboardService.getCategoryBreakdown();
        assertEquals(1, result.size());
        Map<String, Object> entry = result.get(0);
        assertEquals("Salary", entry.get("category"));
        assertEquals(RecordType.INCOME, entry.get("type"));
        assertEquals(new BigDecimal("2000"), entry.get("total"));
    }

    @Test
    void testGetMonthlyTrends() {
        Object[] row = new Object[]{2026, 4, RecordType.INCOME, new BigDecimal("3000")};
        List<Object[]> rows = new ArrayList<>();
        rows.add(row);
        when(recordRepository.monthlyTrends(any(LocalDate.class))).thenReturn(rows);
        List<Map<String, Object>> trends = dashboardService.getMonthlyTrends();
        assertEquals(1, trends.size());
        Map<String, Object> entry = trends.get(0);
        assertEquals(2026, entry.get("year"));
        assertEquals(4, entry.get("month"));
        assertEquals(RecordType.INCOME, entry.get("type"));
        assertEquals(new BigDecimal("3000"), entry.get("total"));
    }

    @Test
    void testGetRecentActivity() {
        // Mock a FinancialRecord entity
        com.finance.entity.FinancialRecord rec = new com.finance.entity.FinancialRecord();
        rec.setId(1L);
        rec.setAmount(new BigDecimal("500.0"));
        rec.setType(RecordType.EXPENSE);
        rec.setCategory("Food");
        rec.setDate(LocalDate.now());
        rec.setNotes("Lunch");
        com.finance.entity.User user = new com.finance.entity.User();
        user.setName("Admin User");
        rec.setUser(user);
        when(recordRepository.findRecentRecords(any())).thenReturn(List.of(rec));
        List<Map<String, Object>> recent = dashboardService.getRecentActivity();
        assertEquals(1, recent.size());
        Map<String, Object> entry = recent.get(0);
        assertEquals(1L, entry.get("id"));
        assertEquals(new BigDecimal("500.0"), entry.get("amount"));
        assertEquals(RecordType.EXPENSE, entry.get("type"));
        assertEquals("Food", entry.get("category"));
        assertEquals("Admin User", entry.get("createdBy"));
    }
}
