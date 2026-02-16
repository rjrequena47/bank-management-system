package com.codebytes5.banking.customers.controller;

import com.codebytes5.banking.customers.dto.AuthResponse;
import com.codebytes5.banking.customers.dto.CustomerRegistrationRequest;
import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.dto.LoginRequest;
import com.codebytes5.banking.customers.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para gestionar el registro y la autenticación de clientes.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para el registro y login de clientes")
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para registrar un nuevo cliente.
     * Crea un cliente con estado ACTIVE y rol CUSTOMER.
     */
    @Operation(summary = "Registrar un nuevo cliente", description = "Crea un nuevo cliente en el sistema con estado ACTIVE")
    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRegistrationRequest request) {
        CustomerResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint para autenticar un cliente y obtener un token JWT.
     * El token es válido por 1 hora.
     */
    @Operation(summary = "Iniciar sesión", description = "Autentica al cliente con email y password, devolviendo un token JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
