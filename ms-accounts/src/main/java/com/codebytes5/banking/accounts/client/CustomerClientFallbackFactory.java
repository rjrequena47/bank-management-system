package com.codebytes5.banking.accounts.client;

import com.codebytes5.banking.accounts.dto.CustomerInfoResponse;
import com.codebytes5.banking.accounts.dto.CustomerValidationResponse;
import com.codebytes5.banking.accounts.exception.CustomerServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fábrica de fallback para {@link CustomerClient}.
 *
 * <p>
 * Se activa cuando ms-customers no está disponible (timeout, conexión
 * rechazada,
 * circuit breaker abierto). Nunca devuelve {@code null} — siempre lanza
 * {@link CustomerServiceUnavailableException} con un mensaje en español para el
 * cliente.
 *
 * <p>
 * El detalle técnico de la causa se registra en el log con nivel ERROR sin
 * exponer el stack trace en la respuesta HTTP.
 */
@Slf4j
@Component
public class CustomerClientFallbackFactory implements FallbackFactory<CustomerClient> {

    private static final String MSG_SERVICIO_NO_DISPONIBLE = "Servicio de clientes no disponible temporalmente. Intente nuevamente más tarde.";

    @Override
    public CustomerClient create(Throwable cause) {
        // Log técnico interno: tipo de error y mensaje — el stack trace no se expone al
        // cliente
        log.error("[CustomerClient] ms-customers no disponible. Causa: {} — {}",
                cause.getClass().getSimpleName(), cause.getMessage());

        return new CustomerClient() {

            @Override
            public CustomerValidationResponse validateCustomer(UUID customerId) {
                log.error("[CustomerClient#validateCustomer] Fallback activado para customerId={}",
                        customerId);
                throw new CustomerServiceUnavailableException(MSG_SERVICIO_NO_DISPONIBLE);
            }

            @Override
            public CustomerInfoResponse getCustomerById(UUID customerId) {
                log.error("[CustomerClient#getCustomerById] Fallback activado para customerId={}",
                        customerId);
                throw new CustomerServiceUnavailableException(MSG_SERVICIO_NO_DISPONIBLE);
            }
        };
    }
}
