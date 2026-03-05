package com.codebytes5.banking.accounts.dto;

import com.codebytes5.banking.accounts.enums.TransferStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para el caso de uso de transferencia bancaria (HU-10).
 */
@Data
@Builder
public class TransferResponse {

    private UUID transferId;
    private String referenceNumber;
    private String sourceAccountNumber;
    private String destinationAccountNumber;

    /**
     * Nombre completo del beneficiario si es cliente interno. Null para IBANs
     * externos.
     */
    private String beneficiaryName;

    private BigDecimal amount;
    private BigDecimal fee;
    private String concept;
    private TransferStatus status;
    private Instant createdAt;
}
