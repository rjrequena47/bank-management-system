package com.codebytes5.banking.accounts.dto;

import com.codebytes5.banking.accounts.enums.AccountStatus;
import com.codebytes5.banking.accounts.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private UUID id;
    private String accountNumber;
    private UUID customerId;
    private AccountType accountType;
    private String currency;
    private BigDecimal balance;
    private String alias;
    private AccountStatus status;
    private BigDecimal dailyWithdrawalLimit;
    private Instant createdAt;
}
