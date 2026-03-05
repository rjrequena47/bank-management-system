package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.client.CustomerClient;
import com.codebytes5.banking.accounts.dto.AccountResponse;
import com.codebytes5.banking.accounts.dto.CreateAccountRequest;
import com.codebytes5.banking.accounts.dto.CustomerValidationResponse;
import com.codebytes5.banking.accounts.enums.AccountType;
import com.codebytes5.banking.accounts.exception.AccountNotFoundException;
import com.codebytes5.banking.accounts.exception.CustomerNotActiveException;
import com.codebytes5.banking.accounts.exception.CustomerNotFoundException;
import com.codebytes5.banking.accounts.exception.MaxAccountsReachedException;
import com.codebytes5.banking.accounts.exception.UnauthorizedAccountAccessException;
import com.codebytes5.banking.accounts.mapper.AccountMapper;
import com.codebytes5.banking.accounts.model.Account;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private CustomerClient customerClient;
    @Mock
    private IbanGeneratorService ibanGeneratorService;

    @InjectMocks
    private AccountService accountService;

    private UUID customerId;
    private CreateAccountRequest createRequest;
    private CustomerValidationResponse validationActive;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        createRequest = CreateAccountRequest.builder()
                .accountType(AccountType.SAVINGS)
                .currency("EUR")
                .alias("My Account")
                .build();
        validationActive = CustomerValidationResponse.builder()
                .customerId(customerId)
                .exists(true)
                .active(true)
                .build();
    }

    @Test
    void createAccount_success() {
        when(customerClient.validateCustomer(customerId)).thenReturn(validationActive);
        when(accountRepository.countByCustomerId(customerId)).thenReturn(0L);
        when(ibanGeneratorService.generateUniqueIban()).thenReturn("ES1234567890");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountMapper.toResponse(any(Account.class))).thenReturn(new AccountResponse());

        AccountResponse response = accountService.createAccount(customerId, createRequest);

        assertNotNull(response);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_customerNotFound() {
        CustomerValidationResponse validationNotFound = CustomerValidationResponse.builder()
                .exists(false)
                .build();
        when(customerClient.validateCustomer(customerId)).thenReturn(validationNotFound);

        assertThrows(CustomerNotFoundException.class, () -> accountService.createAccount(customerId, createRequest));
    }

    @Test
    void createAccount_customerNotActive() {
        CustomerValidationResponse validationInactive = CustomerValidationResponse.builder()
                .exists(true)
                .active(false)
                .build();
        when(customerClient.validateCustomer(customerId)).thenReturn(validationInactive);

        assertThrows(CustomerNotActiveException.class, () -> accountService.createAccount(customerId, createRequest));
    }

    @Test
    void createAccount_maxAccountsReached() {
        when(customerClient.validateCustomer(customerId)).thenReturn(validationActive);
        when(accountRepository.countByCustomerId(customerId)).thenReturn(3L);

        assertThrows(MaxAccountsReachedException.class, () -> accountService.createAccount(customerId, createRequest));
    }

    @Test
    void getAccountsByCustomerId_success() {
        Account account = Account.builder().customerId(customerId).build();
        when(accountRepository.findByCustomerId(customerId)).thenReturn(List.of(account));
        when(accountMapper.toResponse(account)).thenReturn(new AccountResponse());

        List<AccountResponse> result = accountService.getAccountsByCustomerId(customerId);

        assertEquals(1, result.size());
    }

    @Test
    void getAccountByIdAndCustomerId_success() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).customerId(customerId).build();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(new AccountResponse());

        AccountResponse result = accountService.getAccountByIdAndCustomerId(accountId, customerId);

        assertNotNull(result);
    }

    @Test
    void getAccountByIdAndCustomerId_notOwner() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).customerId(UUID.randomUUID()).build();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> accountService.getAccountByIdAndCustomerId(accountId, customerId));
    }

    @Test
    void getAccountByIdAndCustomerId_notFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccountByIdAndCustomerId(accountId, customerId));
    }
}
