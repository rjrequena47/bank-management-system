package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.client.CustomerClient;
import com.codebytes5.banking.accounts.dto.CustomerInfoResponse;
import com.codebytes5.banking.accounts.dto.CustomerValidationResponse;
import com.codebytes5.banking.accounts.dto.TransferRequest;
import com.codebytes5.banking.accounts.dto.TransferResponse;
import com.codebytes5.banking.accounts.enums.AccountStatus;
import com.codebytes5.banking.accounts.enums.TransactionStatus;
import com.codebytes5.banking.accounts.enums.TransactionType;
import com.codebytes5.banking.accounts.enums.TransferStatus;
import com.codebytes5.banking.accounts.exception.AccountNotFoundException;
import com.codebytes5.banking.accounts.exception.CustomerNotActiveException;
import com.codebytes5.banking.accounts.exception.CustomerNotFoundException;
import com.codebytes5.banking.accounts.exception.InsufficientBalanceException;
import com.codebytes5.banking.accounts.exception.InvalidTransactionException;
import com.codebytes5.banking.accounts.exception.UnauthorizedAccountAccessException;
import com.codebytes5.banking.accounts.model.Account;
import com.codebytes5.banking.accounts.model.Transaction;
import com.codebytes5.banking.accounts.model.Transfer;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import com.codebytes5.banking.accounts.repository.TransactionRepository;
import com.codebytes5.banking.accounts.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio que implementa el caso de uso HU-10: Transferencia Bancaria.
 *
 * Flujo transaccional:
 * 1. Validate source account ownership
 * 2. Validate source account is ACTIVE
 * 3. Validate customer is ACTIVE via ms-customers
 * 4. Validate positive amount
 * 5. Validate sufficient balance
 * 6. Resolve destination account (internal vs. external)
 * 7. Resolve beneficiary name (internal only, graceful degradation)
 * 8. Debit source account + create TRANSFER_OUT transaction
 * 9. Credit destination account (if internal) + create TRANSFER_IN transaction
 * 10. Persist Transfer record
 * 11. Return TransferResponse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final CustomerClient customerClient;

    @Transactional
    public TransferResponse executeTransfer(UUID customerId, TransferRequest request) {

        // 1. Validate source account ownership
        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Cuenta origen no encontrada: " + request.getSourceAccountId()));

        if (!sourceAccount.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccountAccessException(
                    "La cuenta origen no pertenece al cliente autenticado");
        }

        // 2. Validate source account is ACTIVE
        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException(
                    "La cuenta origen debe estar activa para realizar una transferencia");
        }

        // 3. Validate customer is ACTIVE via ms-customers
        CustomerValidationResponse validation = customerClient.validateCustomer(customerId);
        if (!validation.isExists()) {
            throw new CustomerNotFoundException(customerId);
        }
        if (!validation.isActive()) {
            throw new CustomerNotActiveException(customerId);
        }

        // 4. Validate amount > 0 (also enforced by @DecimalMin in DTO, but
        // double-check)
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "El monto de la transferencia debe ser mayor a cero");
        }

        // 5. Validate sufficient balance
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Saldo insuficiente para realizar la transferencia. Saldo disponible: "
                            + sourceAccount.getBalance());
        }

        // 6. Resolve destination account (internal or external)
        Optional<Account> destinationAccountOpt = accountRepository
                .findByAccountNumber(request.getDestinationAccountNumber());

        // 7. Resolve beneficiary name (graceful degradation on Feign errors)
        String beneficiaryName = null;
        UUID destinationAccountId = null;

        if (destinationAccountOpt.isPresent()) {
            Account destinationAccount = destinationAccountOpt.get();
            destinationAccountId = destinationAccount.getId();
            beneficiaryName = resolveBeneficiaryName(destinationAccount.getCustomerId());
        }

        // 8. Generate unique transfer reference
        String referenceNumber = "TRF-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();

        // 9. Debit source account
        BigDecimal sourceBalanceBefore = sourceAccount.getBalance();
        sourceAccount.setBalance(sourceBalanceBefore.subtract(request.getAmount()));
        accountRepository.save(sourceAccount);

        Transaction debitTransaction = Transaction.builder()
                .accountId(sourceAccount.getId())
                .type(TransactionType.TRANSFER_OUT)
                .amount(request.getAmount())
                .balanceAfter(sourceAccount.getBalance())
                .concept(request.getConcept())
                .counterpartyAccountNumber(request.getDestinationAccountNumber())
                .counterpartyName(beneficiaryName)
                .referenceNumber(referenceNumber)
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction savedDebit = transactionRepository.save(debitTransaction);

        // 10. Credit destination account (if internal)
        Transaction savedCredit = null;
        if (destinationAccountOpt.isPresent()) {
            Account destinationAccount = destinationAccountOpt.get();

            if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new InvalidTransactionException(
                        "La cuenta destino debe estar activa para recibir una transferencia");
            }

            destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));
            accountRepository.save(destinationAccount);

            // Resolve sender name for credit transaction counterpartyName
            String senderName = resolveBeneficiaryName(sourceAccount.getCustomerId());

            Transaction creditTransaction = Transaction.builder()
                    .accountId(destinationAccount.getId())
                    .type(TransactionType.TRANSFER_IN)
                    .amount(request.getAmount())
                    .balanceAfter(destinationAccount.getBalance())
                    .concept(request.getConcept())
                    .counterpartyAccountNumber(sourceAccount.getAccountNumber())
                    .counterpartyName(senderName)
                    .referenceNumber(referenceNumber)
                    .status(TransactionStatus.COMPLETED)
                    .build();

            savedCredit = transactionRepository.save(creditTransaction);
        }

        // 11. Persist Transfer record
        Transfer transfer = Transfer.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountId(destinationAccountId)
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .concept(request.getConcept())
                .status(TransferStatus.COMPLETED)
                .referenceNumber(referenceNumber)
                .debitTransactionId(savedDebit.getId())
                .creditTransactionId(savedCredit != null ? savedCredit.getId() : null)
                .build();

        Transfer savedTransfer = transferRepository.save(transfer);

        // 12. Return response
        return TransferResponse.builder()
                .transferId(savedTransfer.getId())
                .referenceNumber(referenceNumber)
                .sourceAccountNumber(sourceAccount.getAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .beneficiaryName(beneficiaryName)
                .amount(request.getAmount())
                .concept(request.getConcept())
                .status(TransferStatus.COMPLETED)
                .createdAt(savedTransfer.getCreatedAt())
                .build();
    }

    /**
     * Resuelve el nombre completo de un cliente llamando a ms-customers.
     * Devuelve null si el servicio no está disponible o el cliente no existe.
     */
    private String resolveBeneficiaryName(UUID targetCustomerId) {
        try {
            CustomerInfoResponse info = customerClient.getCustomerById(targetCustomerId);
            if (info != null) {
                return info.getFullName() != null
                        ? info.getFullName()
                        : (info.getFirstName() + " " + info.getLastName()).trim();
            }
        } catch (Exception e) {
            log.warn("No se pudo resolver el nombre del cliente {}: {}", targetCustomerId, e.getMessage());
        }
        return null;
    }
}
