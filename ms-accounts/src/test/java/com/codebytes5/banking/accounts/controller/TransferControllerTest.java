package com.codebytes5.banking.accounts.controller;

import com.codebytes5.banking.accounts.config.JwtService;
import com.codebytes5.banking.accounts.dto.TransferRequest;
import com.codebytes5.banking.accounts.dto.TransferResponse;
import com.codebytes5.banking.accounts.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void transfer_returns201() throws Exception {
        UUID customerId = UUID.randomUUID();
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(UUID.randomUUID());
        request.setDestinationAccountNumber("ES123456");
        request.setAmount(BigDecimal.TEN);
        request.setConcept("Test");

        when(jwtService.extractCustomerId(any())).thenReturn(customerId);
        when(transferService.executeTransfer(eq(customerId), any())).thenReturn(TransferResponse.builder().build());

        mockMvc.perform(post("/api/transfers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
