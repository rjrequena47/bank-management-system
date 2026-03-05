package com.codebytes5.banking.accounts.repository;

import com.codebytes5.banking.accounts.model.Transaction;
import com.codebytes5.banking.accounts.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
        List<Transaction> findByAccountId(UUID accountId);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountId = :accountId AND t.type = :type AND t.createdAt >= :startOfDay AND t.createdAt <= :endOfDay")
        BigDecimal getSumOfTransactionsByTypeAndDateRange(
                        @Param("accountId") UUID accountId,
                        @Param("type") TransactionType type,
                        @Param("startOfDay") Instant startOfDay,
                        @Param("endOfDay") Instant endOfDay);

        @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId " +
                        "AND (cast(:startDate as timestamp) IS NULL OR t.createdAt >= :startDate) " +
                        "AND (cast(:endDate as timestamp) IS NULL OR t.createdAt <= :endDate) " +
                        "AND (:type IS NULL OR t.type = :type)")
        Page<Transaction> findByAccountIdAndFilters(
                        @Param("accountId") UUID accountId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("type") TransactionType type,
                        Pageable pageable);
}
