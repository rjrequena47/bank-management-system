package com.codebytes5.banking.customers.service;

import com.codebytes5.banking.customers.dto.AuthResponse;
import com.codebytes5.banking.customers.dto.CustomerRegistrationRequest;
import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.dto.LoginRequest;
import com.codebytes5.banking.customers.enums.CustomerStatus;
import com.codebytes5.banking.customers.exception.ConflictException;
import com.codebytes5.banking.customers.exception.InvalidCredentialsException;
import com.codebytes5.banking.customers.mapper.CustomerMapper;
import com.codebytes5.banking.customers.model.Customer;
import com.codebytes5.banking.customers.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpiration", 3600000L);
    }

    @Test
    void register_success() {
        CustomerRegistrationRequest request = CustomerRegistrationRequest.builder()
                .email("test@example.com")
                .dni("12345678A")
                .password("Pass123!")
                .build();

        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(customerRepository.existsByDni(request.getDni())).thenReturn(false);
        when(customerMapper.toEntity(request)).thenReturn(new Customer());
        when(passwordEncoder.encode(any())).thenReturn("encodedPass");
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(customerMapper.toResponse(any())).thenReturn(new CustomerResponse());

        CustomerResponse response = authService.register(request);

        assertNotNull(response);
        verify(customerRepository).save(any());
    }

    @Test
    void register_emailExists() {
        CustomerRegistrationRequest request = CustomerRegistrationRequest.builder().email("exists@test.com").build();
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("test@test.com", "pass");
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .password("encoded")
                .status(CustomerStatus.ACTIVE)
                .role(com.codebytes5.banking.customers.enums.UserRole.CUSTOMER)
                .build();

        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(request.getPassword(), customer.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("token", response.getToken());
    }

    @Test
    void login_invalidCredentials() {
        LoginRequest request = new LoginRequest("test@test.com", "wrong");
        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
