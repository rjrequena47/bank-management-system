package com.codebytes5.banking.accounts.controller;

import com.codebytes5.banking.accounts.config.JwtService;
import com.codebytes5.banking.accounts.dto.TransferRequest;
import com.codebytes5.banking.accounts.dto.TransferResponse;
import com.codebytes5.banking.accounts.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para la gestión de transferencias bancarias (HU-10).
 * El customerId se extrae del token JWT, nunca del cuerpo de la petición.
 */
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Endpoints para la gestión de transferencias bancarias")
public class TransferController {

    private final TransferService transferService;
    private final JwtService jwtService;

    /**
     * Ejecuta una transferencia bancaria desde una cuenta del cliente autenticado
     * hacia cualquier cuenta destino (interna o externa).
     *
     * Si la cuenta destino pertenece a un cliente interno del sistema, el nombre
     * completo del beneficiario se incluye en la respuesta.
     */
    @Operation(summary = "Realizar transferencia bancaria", description = "Transfiere fondos desde una cuenta del cliente autenticado a una cuenta destino por IBAN. "
            + "El customerId se extrae del JWT. Si la cuenta destino es interna, se incluye el nombre del beneficiario.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                    @ApiResponse(responseCode = "201", description = "Transferencia realizada con éxito"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o saldo insuficiente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "La cuenta origen no pertenece al cliente"),
                    @ApiResponse(responseCode = "404", description = "Cuenta origen no encontrada")
            })
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @RequestBody @Valid TransferRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();
        UUID customerId = jwtService.extractCustomerId(token);

        TransferResponse response = transferService.executeTransfer(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
