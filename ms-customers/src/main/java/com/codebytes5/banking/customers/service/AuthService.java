package com.codebytes5.banking.customers.service;

import com.codebytes5.banking.customers.dto.AuthResponse;
import com.codebytes5.banking.customers.dto.CustomerRegistrationRequest;
import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.dto.LoginRequest;
import com.codebytes5.banking.customers.enums.CustomerStatus;
import com.codebytes5.banking.customers.enums.UserRole;
import com.codebytes5.banking.customers.mapper.CustomerMapper;
import com.codebytes5.banking.customers.model.Customer;
import com.codebytes5.banking.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codebytes5.banking.customers.exception.ConflictException;
import com.codebytes5.banking.customers.exception.InvalidCredentialsException;

/**
 * Servicio encargado de la gestión de autenticación y registro de clientes.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Registra un nuevo cliente en el sistema.
     * Valida que no existan duplicados de email o DNI.
     */

    @Transactional
    public CustomerResponse register(CustomerRegistrationRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        if (customerRepository.existsByDni(request.getDni())) {
            throw new ConflictException("DNI already exists");
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setRole(UserRole.CUSTOMER);

        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    /**
     * Autentica a un cliente y devuelve un token JWT.
     */
    public AuthResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new ConflictException("Customer account is not active");
        }

        String token = jwtService.generateToken(customer.getId(), customer.getEmail(), customer.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .customerId(customer.getId())
                .build();
    }
}
