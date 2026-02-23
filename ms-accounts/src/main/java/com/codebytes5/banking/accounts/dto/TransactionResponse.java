package com.codebytes5.banking.accounts.dto;

import com.codebytes5.banking.accounts.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class TransactionResponse {
    private UUID transactionId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private Instant timestamp;
}
