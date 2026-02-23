package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.dto.DepositRequest;
import com.codebytes5.banking.accounts.dto.TransactionResponse;
import com.codebytes5.banking.accounts.dto.WithdrawalRequest;
import com.codebytes5.banking.accounts.enums.AccountStatus;
import com.codebytes5.banking.accounts.enums.TransactionStatus;
import com.codebytes5.banking.accounts.enums.TransactionType;
import com.codebytes5.banking.accounts.exception.AccountNotFoundException;
import com.codebytes5.banking.accounts.exception.DailyWithdrawalLimitExceededException;
import com.codebytes5.banking.accounts.exception.InsufficientBalanceException;
import com.codebytes5.banking.accounts.exception.InvalidTransactionException;
import com.codebytes5.banking.accounts.exception.UnauthorizedAccountAccessException;
import com.codebytes5.banking.accounts.mapper.TransactionMapper;
import com.codebytes5.banking.accounts.model.Account;
import com.codebytes5.banking.accounts.model.Transaction;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import com.codebytes5.banking.accounts.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse deposit(UUID accountId, UUID customerId, DepositRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada"));

        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccountAccessException("La cuenta no pertenece al cliente autenticado");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("La cuenta debe estar activa para realizar un depósito");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("El monto a depositar debe ser mayor a cero");
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(account.getBalance())
                .concept(request.getDescription())
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public TransactionResponse withdraw(UUID accountId, UUID customerId, WithdrawalRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada"));

        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccountAccessException("La cuenta no pertenece al cliente autenticado");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("La cuenta debe estar activa para realizar un retiro");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("El monto a retirar debe ser mayor a cero");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar el retiro");
        }

        Instant now = Instant.now();
        ZonedDateTime zdt = now.atZone(ZoneId.of("UTC"));
        Instant startOfDay = zdt.toLocalDate().atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant endOfDay = startOfDay.plusSeconds(86400).minusNanos(1);

        BigDecimal withdrawnToday = transactionRepository.getSumOfTransactionsByTypeAndDateRange(
                accountId, TransactionType.WITHDRAWAL, startOfDay, endOfDay);

        if (withdrawnToday == null) {
            withdrawnToday = BigDecimal.ZERO;
        }

        BigDecimal newTotalWithdrawnToday = withdrawnToday.add(request.getAmount());

        BigDecimal limit = account.getDailyWithdrawalLimit() != null
                ? account.getDailyWithdrawalLimit()
                : BigDecimal.valueOf(1000.00);

        if (newTotalWithdrawnToday.compareTo(limit) > 0) {
            throw new DailyWithdrawalLimitExceededException(
                    String.format(
                            "Límite de retiro diario excedido. Límite: %s, Ya retirado hoy: %s, Intento actual: %s",
                            limit, withdrawnToday, request.getAmount()));
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceAfter(account.getBalance())
                .concept(request.getDescription())
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }
}
