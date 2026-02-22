package com.codebytes5.banking.accounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerValidationResponse {

    private UUID customerId;
    private boolean exists;

    /**
     * Renamed from 'isActive' to 'active' to avoid Lombok generating
     * 'isIsActive()'.
     * 
     * @JsonProperty maps the JSON field "isActive" from ms-customers response
     *               correctly.
     */
    @JsonProperty("isActive")
    private boolean active;
}
