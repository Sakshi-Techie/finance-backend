package com.zorvyn.finance.repository;

import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Find active record by ID (soft delete aware)
    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    // Paginated listing with filters
    @Query("SELECT f FROM FinancialRecord f WHERE f.deleted = false " +
           "AND (:type IS NULL OR f.type = :type) " +
           "AND (:category IS NULL OR LOWER(f.category) LIKE LOWER(CONCAT('%', :category, '%'))) " +
           "AND (:startDate IS NULL OR f.date >= :startDate) " +
           "AND (:endDate IS NULL OR f.date <= :endDate)")
    Page<FinancialRecord> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Dashboard: total income
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.deleted = false AND f.type = 'INCOME'")
    BigDecimal getTotalIncome();

    // Dashboard: total expenses
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.deleted = false AND f.type = 'EXPENSE'")
    BigDecimal getTotalExpenses();

    // Dashboard: category-wise totals
    @Query("SELECT f.category, f.type, SUM(f.amount) FROM FinancialRecord f WHERE f.deleted = false GROUP BY f.category, f.type")
    List<Object[]> getCategoryWiseTotals();

    // Dashboard: recent records
    @Query("SELECT f FROM FinancialRecord f WHERE f.deleted = false ORDER BY f.createdAt DESC")
    List<FinancialRecord> findRecentActivity(Pageable pageable);

    // Dashboard: monthly trends
    @Query("SELECT YEAR(f.date), MONTH(f.date), f.type, SUM(f.amount) " +
           "FROM FinancialRecord f WHERE f.deleted = false " +
           "GROUP BY YEAR(f.date), MONTH(f.date), f.type " +
           "ORDER BY YEAR(f.date) DESC, MONTH(f.date) DESC")
    List<Object[]> getMonthlyTrends();
}
