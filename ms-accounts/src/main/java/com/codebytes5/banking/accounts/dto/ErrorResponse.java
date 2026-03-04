package com.codebytes5.banking.accounts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de respuesta de error estándar para todos los endpoints de ms-accounts.
 *
 * Campos:
 * 
 * {@code timestamp} — momento del error
 * {@code status} — código HTTP
 * {@code errorCode} — código legible para logs/soporte
 * (ej.CUENTA_NO_ENCONTRADA)
 * {@code message} — mensaje en español para el cliente final
 * {@code path} — URI del request que originó el error
 *
 * El stack trace NUNCA se incluye en esta respuesta.
 */
@Getter
@Builder
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    private final int status;

    private final String errorCode;

    private final String message;

    private final String path;
}
