package com.codebytes5.banking.accounts.controller;

import com.codebytes5.banking.accounts.config.JwtService;
import com.codebytes5.banking.accounts.dto.AccountResponse;
import com.codebytes5.banking.accounts.dto.CreateAccountRequest;
import com.codebytes5.banking.accounts.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST para la gestión de cuentas bancarias.
 * El customerId se extrae del token JWT (claim "customerId"), nunca del cuerpo
 * de la petición.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Endpoints para la gestión de cuentas bancarias")
public class AccountController {

    private final AccountService accountService;
    private final JwtService jwtService;

    /**
     * Crea una nueva cuenta bancaria para el cliente autenticado.
     * El customerId se extrae del claim "customerId" del token JWT.
     * El token es leído desde las credenciales del contexto de seguridad,
     * donde fue almacenado por el JwtAuthenticationFilter tras su validación.
     */
    @Operation(summary = "Crear cuenta bancaria", description = "Crea una nueva cuenta bancaria. El customerId se extrae del JWT, nunca del body.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal String email,
            @RequestBody @Valid CreateAccountRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();
        UUID customerId = jwtService.extractCustomerId(token);

        AccountResponse response = accountService.createAccount(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
