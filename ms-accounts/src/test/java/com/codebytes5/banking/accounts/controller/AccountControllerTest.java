package com.codebytes5.banking.accounts.controller;

import com.codebytes5.banking.accounts.config.JwtService;
import com.codebytes5.banking.accounts.dto.AccountResponse;
import com.codebytes5.banking.accounts.dto.CreateAccountRequest;
import com.codebytes5.banking.accounts.enums.AccountType;
import com.codebytes5.banking.accounts.service.AccountService;
import com.codebytes5.banking.accounts.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createAccount_returns201() throws Exception {
        UUID customerId = UUID.randomUUID();
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountType(AccountType.SAVINGS)
                .currency("EUR")
                .build();

        // Simulate JWT extraction
        when(jwtService.extractCustomerId(any())).thenReturn(customerId);
        when(accountService.createAccount(eq(customerId), any())).thenReturn(new AccountResponse());

        mockMvc.perform(post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_VALUE) // Fixed MediaType
                .header("Authorization", "Bearer token")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void getMyAccounts_returns200() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(jwtService.extractCustomerId(any())).thenReturn(customerId);
        when(accountService.getAccountsByCustomerId(customerId)).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getAccountById_returns200() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(jwtService.extractCustomerId(any())).thenReturn(customerId);
        when(accountService.getAccountByIdAndCustomerId(accountId, customerId)).thenReturn(new AccountResponse());

        mockMvc.perform(get("/api/accounts/" + accountId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }
}
