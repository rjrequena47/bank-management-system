package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.client.CustomerClient;
import com.codebytes5.banking.accounts.dto.CustomerValidationResponse;
import com.codebytes5.banking.accounts.dto.TransferRequest;
import com.codebytes5.banking.accounts.dto.TransferResponse;
import com.codebytes5.banking.accounts.enums.AccountStatus;
import com.codebytes5.banking.accounts.enums.TransferStatus;
import com.codebytes5.banking.accounts.exception.InsufficientBalanceException;
import com.codebytes5.banking.accounts.exception.UnauthorizedAccountAccessException;
import com.codebytes5.banking.accounts.model.Account;
import com.codebytes5.banking.accounts.model.Transaction;
import com.codebytes5.banking.accounts.model.Transfer;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import com.codebytes5.banking.accounts.repository.TransactionRepository;
import com.codebytes5.banking.accounts.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private TransferService transferService;

    private UUID customerId;
    private UUID sourceAccountId;
    private String destinationIban;
    private Account sourceAccount;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationIban = "ES9876543210";
        sourceAccount = Account.builder()
                .id(sourceAccountId)
                .customerId(customerId)
                .accountNumber("ES1234567890")
                .balance(BigDecimal.valueOf(1000.00))
                .status(AccountStatus.ACTIVE)
                .build();
        request = new TransferRequest();
        request.setSourceAccountId(sourceAccountId);
        request.setDestinationAccountNumber(destinationIban);
        request.setAmount(BigDecimal.valueOf(200.00));
        request.setConcept("Rent");
    }

    @Test
    void executeTransfer_internalSuccess() {
        Account destinationAccount = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber(destinationIban)
                .balance(BigDecimal.valueOf(100.00))
                .status(AccountStatus.ACTIVE)
                .customerId(UUID.randomUUID())
                .build();

        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(customerClient.validateCustomer(customerId))
                .thenReturn(CustomerValidationResponse.builder().exists(true).active(true).build());
        when(accountRepository.findByAccountNumber(destinationIban)).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(i -> i.getArgument(0));

        TransferResponse response = transferService.executeTransfer(customerId, request);

        assertNotNull(response);
        assertEquals(TransferStatus.COMPLETED, response.getStatus());
        assertEquals(0, BigDecimal.valueOf(800.00).compareTo(sourceAccount.getBalance()));
        assertEquals(0, BigDecimal.valueOf(300.00).compareTo(destinationAccount.getBalance()));
    }

    @Test
    void executeTransfer_externalSuccess() {
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(customerClient.validateCustomer(customerId))
                .thenReturn(CustomerValidationResponse.builder().exists(true).active(true).build());
        when(accountRepository.findByAccountNumber(destinationIban)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(i -> i.getArgument(0));

        TransferResponse response = transferService.executeTransfer(customerId, request);

        assertNotNull(response);
        assertEquals(0, BigDecimal.valueOf(800.00).compareTo(sourceAccount.getBalance()));
    }

    @Test
    void executeTransfer_insufficientBalance() {
        request.setAmount(BigDecimal.valueOf(5000.00));
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(customerClient.validateCustomer(customerId))
                .thenReturn(CustomerValidationResponse.builder().exists(true).active(true).build());

        assertThrows(InsufficientBalanceException.class, () -> transferService.executeTransfer(customerId, request));
    }

    @Test
    void executeTransfer_notOwner() {
        sourceAccount.setCustomerId(UUID.randomUUID());
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> transferService.executeTransfer(customerId, request));
    }
}
