package com.codebytes5.banking.customers.controller;

import com.codebytes5.banking.customers.service.JwtService;
import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.dto.UpdateCustomerRequest;
import com.codebytes5.banking.customers.service.AuthService;
import com.codebytes5.banking.customers.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test@test.com")
    void getMyProfile_returns200() throws Exception {
        when(customerService.getProfile("test@test.com")).thenReturn(new CustomerResponse());

        mockMvc.perform(get("/api/customers/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void updateProfile_returns200() throws Exception {
        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .build();
        when(customerService.updateProfile(eq("test@test.com"), any())).thenReturn(new CustomerResponse());

        mockMvc.perform(put("/api/customers/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getCustomerById_internal_returns200() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.getCustomerById(customerId)).thenReturn(new CustomerResponse());

        mockMvc.perform(get("/api/customers/" + customerId)
                .header("X-Internal-Service", "ms-accounts"))
                .andExpect(status().isOk());
    }
}
