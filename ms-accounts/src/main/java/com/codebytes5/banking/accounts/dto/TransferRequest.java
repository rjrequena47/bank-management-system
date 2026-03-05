package com.codebytes5.banking.accounts.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de entrada para el caso de uso de transferencia bancaria (HU-10).
 * El customerId se extrae del JWT, nunca del body.
 */
@Data
public class TransferRequest {

    @NotNull(message = "Source account ID is required")
    private UUID sourceAccountId;

    @NotBlank(message = "Destination account number is required")
    private String destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @DecimalMax(value = "10000.00", message = "Maximum transfer amount is 10,000.00")
    private BigDecimal amount;

    @NotBlank(message = "Concept is required")
    private String concept;
}
