package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.dto.DepositRequest;
import com.codebytes5.banking.accounts.dto.TransactionResponse;
import com.codebytes5.banking.accounts.dto.WithdrawalRequest;
import com.codebytes5.banking.accounts.enums.AccountStatus;
import com.codebytes5.banking.accounts.enums.TransactionType;
import com.codebytes5.banking.accounts.exception.DailyWithdrawalLimitExceededException;
import com.codebytes5.banking.accounts.exception.InsufficientBalanceException;
import com.codebytes5.banking.accounts.exception.UnauthorizedAccountAccessException;
import com.codebytes5.banking.accounts.mapper.TransactionMapper;
import com.codebytes5.banking.accounts.model.Account;
import com.codebytes5.banking.accounts.model.Transaction;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import com.codebytes5.banking.accounts.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private UUID customerId;
    private UUID accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .balance(BigDecimal.valueOf(1000.00))
                .status(AccountStatus.ACTIVE)
                .dailyWithdrawalLimit(BigDecimal.valueOf(1000.00))
                .build();
    }

    @Test
    void deposit_success() {
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(500.00), "Deposit");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(new TransactionResponse());

        TransactionResponse response = transactionService.deposit(accountId, customerId, request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(1500.00), account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void deposit_notOwner() {
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(500.00), "Deposit");
        account.setCustomerId(UUID.randomUUID());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> transactionService.deposit(accountId, customerId, request));
    }

    @Test
    void withdraw_success() {
        WithdrawalRequest request = new WithdrawalRequest(BigDecimal.valueOf(200.00), "Withdraw");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.getSumOfTransactionsByTypeAndDateRange(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(new TransactionResponse());

        TransactionResponse response = transactionService.withdraw(accountId, customerId, request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(800.00), account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void withdraw_insufficientBalance() {
        WithdrawalRequest request = new WithdrawalRequest(BigDecimal.valueOf(2000.00), "Withdraw");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class,
                () -> transactionService.withdraw(accountId, customerId, request));
    }

    @Test
    void withdraw_dailyLimitExceeded() {
        WithdrawalRequest request = new WithdrawalRequest(BigDecimal.valueOf(600.00), "Withdraw");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        // Ya retiró 500 hoy, intenta sacar 600 -> total 1100 > 1000 (límite)
        when(transactionRepository.getSumOfTransactionsByTypeAndDateRange(any(), eq(TransactionType.WITHDRAWAL), any(),
                any()))
                .thenReturn(BigDecimal.valueOf(500.00));

        assertThrows(DailyWithdrawalLimitExceededException.class,
                () -> transactionService.withdraw(accountId, customerId, request));
    }

    @Test
    void getTransactionsByAccount_success() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        Page<Transaction> page = new PageImpl<>(List.of(new Transaction()));
        when(transactionRepository.findByAccountIdAndFilters(eq(accountId), any(), any(), any(),
                any(Pageable.class))).thenReturn(page);
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(new TransactionResponse());

        Page<TransactionResponse> result = transactionService.getTransactionsByAccount(accountId, customerId, null,
                null, null, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getSize());
    }
}
