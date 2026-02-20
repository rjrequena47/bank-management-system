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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.util.List;
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
            @RequestBody @Valid CreateAccountRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();
        UUID customerId = jwtService.extractCustomerId(token);

        AccountResponse response = accountService.createAccount(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener mis cuentas bancarias", description = "Retorna todas las cuentas del cliente autenticado. El customerId se extrae del token JWT.", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de cuentas del cliente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AccountResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado - Token inválido o ausente"),
            @ApiResponse(responseCode = "403", description = "No autorizado - Token no tiene permisos")
    })

    @GetMapping("/my-accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();
        UUID customerId = jwtService.extractCustomerId(token);
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Obtener detalle de cuenta específica", description = "Retorna los detalles completos de una cuenta específica. Valida que la cuenta pertenezca al cliente autenticado.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Detalle de la cuenta"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al cliente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable UUID accountId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();
        UUID customerId = jwtService.extractCustomerId(token);

        AccountResponse account = accountService.getAccountByIdAndCustomerId(accountId, customerId);

        return ResponseEntity.ok(account);
    }
}
