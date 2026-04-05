package com.finance.service;

import com.finance.enums.RecordType;
import com.finance.repository.FinancialRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public DashboardService(FinancialRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public Map<String, Object> getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netBalance", netBalance);
        return summary;
    }

    public List<Map<String, Object>> getCategoryBreakdown() {
        List<Object[]> rows = recordRepository.categoryBreakdown();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("category", row[0]);
            entry.put("type", row[1]);
            entry.put("total", row[2]);
            result.add(entry);
        }
        return result;
    }

    public List<Map<String, Object>> getMonthlyTrends() {
        LocalDate since = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        List<Object[]> rows = recordRepository.monthlyTrends(since);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("year", row[0]);
            entry.put("month", row[1]);
            entry.put("type", row[2]);
            entry.put("total", row[3]);
            result.add(entry);
        }
        return result;
    }

    public List<Map<String, Object>> getRecentActivity() {
        return recordRepository.findRecentRecords(PageRequest.of(0, 10))
                .stream()
                .map(r -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("id", r.getId());
                    entry.put("amount", r.getAmount());
                    entry.put("type", r.getType());
                    entry.put("category", r.getCategory());
                    entry.put("date", r.getDate());
                    entry.put("notes", r.getNotes());
                    entry.put("createdBy", r.getUser().getName());
                    return entry;
                })
                .toList();
    }
}
