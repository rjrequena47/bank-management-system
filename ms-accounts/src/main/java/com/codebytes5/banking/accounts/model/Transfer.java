package com.codebytes5.banking.accounts.model;

import com.codebytes5.banking.accounts.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa una transferencia bancaria.
 * Agrupa los dos legs de la transferencia (débito y crédito) bajo un
 * único registro trazable con número de referencia único.
 */
@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Cuenta origen del cliente autenticado. */
    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    /** Cuenta destino si es interna (puede ser null para IBANs externos). */
    @Column(name = "destination_account_id")
    private UUID destinationAccountId;

    /** IBAN destino (siempre presente). */
    @Column(name = "destination_account_number", nullable = false, length = 34)
    private String destinationAccountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(length = 255)
    private String concept;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    /** Identificador externo único de la transferencia (TRF-XXXXXXXX). */
    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    private String referenceNumber;

    /** FK al registro de transacción de débito en la cuenta origen. */
    @Column(name = "debit_transaction_id")
    private UUID debitTransactionId;

    /**
     * FK al registro de transacción de crédito en la cuenta destino (null si
     * externa).
     */
    @Column(name = "credit_transaction_id")
    private UUID creditTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
