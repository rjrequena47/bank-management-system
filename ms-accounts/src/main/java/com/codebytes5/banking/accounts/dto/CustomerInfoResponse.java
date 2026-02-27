package com.codebytes5.banking.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO que mirror el CustomerResponse de ms-customers.
 * Usado por CustomerClient (Feign) para resolver el nombre del beneficiario
 * cuando la cuenta destino pertenece a un cliente interno.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfoResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
}
