package com.finance.repository;

import com.finance.entity.FinancialRecord;
import com.finance.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    // -------------------------------------------------------
    // Dashboard: totals by type
    // -------------------------------------------------------
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = :type")
    BigDecimal sumByType(@Param("type") RecordType type);

    // -------------------------------------------------------
    // Dashboard: category breakdown
    // -------------------------------------------------------
    @Query("SELECT r.category AS category, r.type AS type, SUM(r.amount) AS total " +
           "FROM FinancialRecord r GROUP BY r.category, r.type ORDER BY r.category")
    List<Object[]> categoryBreakdown();

    // -------------------------------------------------------
    // Dashboard: monthly trends (last 12 months)
    // -------------------------------------------------------
    @Query("SELECT FUNCTION('YEAR', r.date) AS yr, FUNCTION('MONTH', r.date) AS mo, " +
           "r.type AS type, SUM(r.amount) AS total " +
           "FROM FinancialRecord r " +
           "WHERE r.date >= :since " +
           "GROUP BY FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type " +
           "ORDER BY yr, mo")
    List<Object[]> monthlyTrends(@Param("since") LocalDate since);

    // -------------------------------------------------------
    // Dashboard: recent records
    // -------------------------------------------------------
    @Query("SELECT r FROM FinancialRecord r ORDER BY r.date DESC, r.createdAt DESC")
    List<FinancialRecord> findRecentRecords(Pageable pageable);
}
