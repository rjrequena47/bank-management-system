package com.codebytes5.banking.accounts.controller;

import com.codebytes5.banking.accounts.config.JwtService;
import com.codebytes5.banking.accounts.dto.AccountResponse;
import com.codebytes5.banking.accounts.dto.CreateAccountRequest;
import com.codebytes5.banking.accounts.dto.DepositRequest;
import com.codebytes5.banking.accounts.dto.TransactionResponse;
import com.codebytes5.banking.accounts.dto.WithdrawalRequest;
import com.codebytes5.banking.accounts.service.AccountService;
import com.codebytes5.banking.accounts.service.TransactionService;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
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
        private final TransactionService transactionService;
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

        @GetMapping
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

        @Operation(summary = "Realizar un depósito", description = "Realiza un depósito en una cuenta específica. Valida que la cuenta pertenezca al cliente autenticado y esté activa.", security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping("/{accountId}/deposit")
        public ResponseEntity<TransactionResponse> deposit(
                        @PathVariable java.util.UUID accountId,
                        @RequestBody @Valid DepositRequest request) {

                System.out.println("REACHED DEPOSIT ENDPOINT FOR ACCOUNT: " + accountId);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String token = (String) authentication.getCredentials();
                java.util.UUID customerId = jwtService.extractCustomerId(token);

                TransactionResponse response = transactionService.deposit(accountId, customerId, request);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Realizar un retiro", description = "Realiza un retiro de una cuenta específica. Valida que la cuenta pertenezca al cliente, esté activa, tenga fondos suficientes y no exceda el límite diario.", security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping("/{accountId}/withdraw")
        public ResponseEntity<TransactionResponse> withdraw(
                        @PathVariable java.util.UUID accountId,
                        @RequestBody @Valid WithdrawalRequest request) {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String token = (String) authentication.getCredentials();
                java.util.UUID customerId = jwtService.extractCustomerId(token);

                TransactionResponse response = transactionService.withdraw(accountId, customerId, request);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Obtener historial de movimientos", description = "Retorna el historial de transacciones de una cuante con paginacion y filtros. Solo accesible por el dueño de la cuenta.", security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/{accountId}/transactions")
        public ResponseEntity<org.springframework.data.domain.Page<TransactionResponse>> getAccountTransactions(
                        @PathVariable UUID accountId,
                        @RequestParam(required = false) Instant startDate,
                        @RequestParam(required = false) Instant endDate,
                        @RequestParam(required = false) com.codebytes5.banking.accounts.enums.TransactionType type,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDirection) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String token = (String) authentication.getCredentials();
                UUID customerId = jwtService.extractCustomerId(token);

                org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort
                                .by(org.springframework.data.domain.Sort.Direction.fromString(sortDirection), sortBy);
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                                size,
                                sort);
                org.springframework.data.domain.Page<TransactionResponse> transactions = transactionService
                                .getTransactionsByAccount(accountId, customerId, startDate, endDate, type, pageable);
                return ResponseEntity.ok(transactions);
        };
}
