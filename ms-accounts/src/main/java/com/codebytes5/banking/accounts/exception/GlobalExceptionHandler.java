package com.codebytes5.banking.accounts.exception;

import com.codebytes5.banking.accounts.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para ms-accounts.
 *
 * Toda respuesta de error sigue la estructura {@link ErrorResponse}:
 * timestamp, status, errorCode, message, path.
 * El stack trace NUNCA se incluye en la respuesta HTTP.
 * Cada handler registra la excepción completa en el log interno.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Validación de entrada
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String details = ex.getBindingResult().getAllErrors().stream()
                .map(e -> {
                    String field = (e instanceof FieldError fe) ? fe.getField() : e.getObjectName();
                    return field + ": " + e.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));

        log.warn("[GlobalExceptionHandler] Error de validación en {}: {}",
                request.getRequestURI(), details);

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Datos de entrada inválidos: " + details, request);
    }

    // -------------------------------------------------------------------------
    // Not Found — 404
    // -------------------------------------------------------------------------

    @ExceptionHandler({ CustomerNotFoundException.class, AccountNotFoundException.class,
            TransferNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("[GlobalExceptionHandler] Recurso no encontrado en {}: {}",
                request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // Forbidden — 403
    // -------------------------------------------------------------------------

    @ExceptionHandler({ CustomerNotActiveException.class, UnauthorizedAccountAccessException.class })
    public ResponseEntity<ErrorResponse> handleForbiddenExceptions(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("[GlobalExceptionHandler] Acceso denegado en {}: {}",
                request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // Conflict — 409
    // -------------------------------------------------------------------------

    @ExceptionHandler(MaxAccountsReachedException.class)
    public ResponseEntity<ErrorResponse> handleConflictExceptions(
            MaxAccountsReachedException ex, HttpServletRequest request) {

        log.warn("[GlobalExceptionHandler] Conflicto de negocio en {}: {}",
                request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.CONFLICT, "LIMITE_CUENTAS_ALCANZADO", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // Bad Request — 400 (transacciones y operaciones inválidas)
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransactionExceptions(
            InvalidTransactionException ex, HttpServletRequest request) {

        log.warn("[GlobalExceptionHandler] Transacción inválida en {}: {}",
                request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.BAD_REQUEST, "TRANSACCION_INVALIDA", ex.getMessage(), request);
    }

    @ExceptionHandler({ InsufficientBalanceException.class,
            DailyWithdrawalLimitExceededException.class })
    public ResponseEntity<ErrorResponse> handleWithdrawalExceptions(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("[GlobalExceptionHandler] Error de retiro en {}: {}",
                request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.BAD_REQUEST, "ERROR_RETIRO", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // Errores de servicios externos (downstream)
    // -------------------------------------------------------------------------

    /**
     * ms-customers no responde (timeout / circuit breaker / connection refused).
     * Mensaje genérico al cliente — detalles técnicos solo en el log.
     */
    @ExceptionHandler(CustomerServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(
            CustomerServiceUnavailableException ex, HttpServletRequest request) {

        log.error("[GlobalExceptionHandler] Servicio externo no disponible en {}: {}",
                request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE",
                ex.getMessage(), request);
    }

    /**
     * ms-customers devolvió un error inesperado (HTTP 5xx u otro no mapeado).
     */
    @ExceptionHandler(CustomerServiceException.class)
    public ResponseEntity<ErrorResponse> handleCustomerServiceError(
            CustomerServiceException ex, HttpServletRequest request) {

        log.error("[GlobalExceptionHandler] Error en servicio externo en {}: {}",
                request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.BAD_GATEWAY, "ERROR_SERVICIO_EXTERNO",
                ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // Fallback genérico — captura cualquier excepción no manejada antes
    // -------------------------------------------------------------------------

    /**
     * Fallback de último recurso. Registra la excepción completa (con stack trace)
     * en el log para diagnóstico, pero NUNCA la expone en la respuesta HTTP.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, HttpServletRequest request) {

        log.error("[GlobalExceptionHandler] Excepción no controlada en {}: tipo={}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Contacte al administrador.", request);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String errorCode, String message, HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(body, status);
    }
}
