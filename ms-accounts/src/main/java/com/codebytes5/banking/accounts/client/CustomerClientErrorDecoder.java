package com.codebytes5.banking.accounts.client;

import com.codebytes5.banking.accounts.exception.CustomerNotFoundException;
import com.codebytes5.banking.accounts.exception.CustomerNotActiveException;
import com.codebytes5.banking.accounts.exception.CustomerServiceException;
import com.codebytes5.banking.accounts.exception.CustomerServiceUnavailableException;
import com.codebytes5.banking.accounts.exception.InvalidTransactionException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Decodificador de errores Feign para {@link CustomerClient}.
 *
 * <p>
 * Intercepta todas las respuestas HTTP de error provenientes de ms-customers y
 * las
 * convierte en excepciones del dominio con mensajes en español para el cliente
 * final.
 * Los detalles técnicos se registran únicamente en los logs internos, sin
 * exponerse
 * en las respuestas HTTP.
 *
 * Mapeo de códigos HTTP:
 * 
 * 400 → {@link InvalidTransactionException} (HTTP 400)
 * 403 → {@link CustomerNotActiveException} (HTTP 403)
 * 404 → {@link CustomerNotFoundException} (HTTP 404)
 * 500 → {@link CustomerServiceException} (HTTP 502)
 * 502, 503 → {@link CustomerServiceUnavailableException} (HTTP 503)
 * Otros → {@link CustomerServiceException} (HTTP 502)
 */
@Slf4j
public class CustomerClientErrorDecoder implements ErrorDecoder {

    private static final String MSG_SOLICITUD_INVALIDA = "Solicitud inválida enviada al servicio externo.";
    private static final String MSG_SIN_PERMISOS = "El cliente no está activo. Por favor contacte con soporte.";
    private static final String MSG_NO_ENCONTRADO = "No se encontró el cliente asociado a esta operación.";
    private static final String MSG_ERROR_INTERNO = "Ocurrió un error al comunicarse con el servicio de clientes.";
    private static final String MSG_NO_DISPONIBLE = "El servicio de clientes no está disponible en este momento. Intente más tarde.";

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String requestUrl = response.request() != null ? response.request().url() : "desconocida";

        // Log técnico interno — no se expone al cliente
        log.warn("[CustomerClient] Error HTTP {} al invocar '{}' → URL: {}",
                status, methodKey, requestUrl);

        return switch (status) {
            case 400 -> {
                log.warn("[CustomerClient] Solicitud inválida rechazada por ms-customers [método={}]",
                        methodKey);
                yield new InvalidTransactionException(MSG_SOLICITUD_INVALIDA);
            }
            case 403 -> {
                log.warn(
                        "[CustomerClient] ms-customers rechazó el acceso — cliente inactivo o sin permisos [método={}]",
                        methodKey);
                yield new CustomerNotActiveException(MSG_SIN_PERMISOS);
            }
            case 404 -> {
                log.warn("[CustomerClient] Recurso no encontrado en ms-customers [método={}]",
                        methodKey);
                yield new CustomerNotFoundException(MSG_NO_ENCONTRADO);
            }
            case 500 -> {
                log.error("[CustomerClient] Error interno en ms-customers (HTTP 500) [método={}]",
                        methodKey);
                yield new CustomerServiceException(MSG_ERROR_INTERNO);
            }
            case 502, 503 -> {
                log.error("[CustomerClient] ms-customers no disponible (HTTP {}) [método={}]",
                        status, methodKey);
                yield new CustomerServiceUnavailableException(MSG_NO_DISPONIBLE);
            }
            default -> {
                log.error("[CustomerClient] Error inesperado HTTP {} desde ms-customers [método={}]",
                        status, methodKey);
                yield new CustomerServiceException(MSG_ERROR_INTERNO);
            }
        };
    }
}
