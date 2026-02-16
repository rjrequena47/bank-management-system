package com.codebytes5.banking.customers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO optimizado para validación interna entre microservicios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerValidationResponse {
    private UUID customerId;
    private boolean exists;
    private boolean isActive;
}
