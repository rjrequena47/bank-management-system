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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse deposit(UUID accountId, UUID customerId, DepositRequest request) {
        log.info("[TransactionService] Iniciando depósito. accountId={}, customerId={}", accountId, customerId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService] Intento de depósito en cuenta inexistente. accountId={}", accountId);
                    return new AccountNotFoundException("Cuenta no encontrada");
                });

        if (!account.getCustomerId().equals(customerId)) {
            log.warn(
                    "[TransactionService] Depósito denegado: cuenta no pertenece al cliente. accountId={}, customerId={}",
                    accountId, customerId);
            throw new UnauthorizedAccountAccessException("La cuenta no pertenece al cliente autenticado");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("[TransactionService] Depósito denegado: cuenta inactiva. accountId={}, status={}", accountId,
                    account.getStatus());
            throw new InvalidTransactionException("La cuenta debe estar activa para realizar un depósito");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[TransactionService] Depósito denegado: monto inválido. accountId={}", accountId);
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
        log.info("[TransactionService] Depósito completado exitosamente. transactionId={}, accountId={}",
                savedTransaction.getId(), accountId);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public TransactionResponse withdraw(UUID accountId, UUID customerId, WithdrawalRequest request) {
        log.info("[TransactionService] Iniciando retiro. accountId={}, customerId={}", accountId, customerId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService] Intento de retiro en cuenta inexistente. accountId={}", accountId);
                    return new AccountNotFoundException("Cuenta no encontrada");
                });

        if (!account.getCustomerId().equals(customerId)) {
            log.warn(
                    "[TransactionService] Retiro denegado: cuenta no pertenece al cliente. accountId={}, customerId={}",
                    accountId, customerId);
            throw new UnauthorizedAccountAccessException("La cuenta no pertenece al cliente autenticado");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("[TransactionService] Retiro denegado: cuenta inactiva. accountId={}, status={}", accountId,
                    account.getStatus());
            throw new InvalidTransactionException("La cuenta debe estar activa para realizar un retiro");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[TransactionService] Retiro denegado: monto inválido. accountId={}", accountId);
            throw new InvalidTransactionException("El monto a retirar debe ser mayor a cero");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("[TransactionService] Retiro denegado: fondos insuficientes. accountId={}", accountId);
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
            log.warn("[TransactionService] Retiro denegado: supera límite diario. accountId={}", accountId);
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
        log.info("[TransactionService] Retiro completado exitosamente. transactionId={}, accountId={}",
                savedTransaction.getId(), accountId);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByAccount(
            UUID accountId,
            UUID customerId,
            Instant startDate,
            Instant endDate,
            TransactionType type,
            Pageable pageable) {
        log.info("[TransactionService] Consultando transacciones. accountId={}, customerId={}", accountId, customerId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService] Consulta transacciones en cuenta inexistente. accountId={}",
                            accountId);
                    return new AccountNotFoundException("Cuenta no encontrada");
                });

        if (!account.getCustomerId().equals(customerId)) {
            log.warn(
                    "[TransactionService] Consulta denegada: cuenta no pertenece al cliente. accountId={}, customerId={}",
                    accountId, customerId);
            throw new UnauthorizedAccountAccessException("La cuenta no pertenece al cliente autenticado");
        }

        Page<Transaction> transactionsPage = transactionRepository.findByAccountIdAndCreatedAtBetweenAndType(accountId,
                startDate, endDate, type, pageable);

        log.info("[TransactionService] Transacciones encontradas. accountId={}, total={}", accountId,
                transactionsPage.getTotalElements());
        return transactionsPage.map(transactionMapper::toResponse);
    }
}
