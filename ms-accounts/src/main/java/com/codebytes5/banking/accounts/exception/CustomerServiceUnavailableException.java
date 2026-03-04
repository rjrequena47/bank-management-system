package com.codebytes5.banking.accounts.exception;

/**
 * Excepción lanzada cuando ms-customers no está disponible (timeout, conexión
 * rechazada,
 * o HTTP 502/503). Se mapea a HTTP 503 Service Unavailable para comunicar al
 * cliente
 * que el servicio externo no responde, sin exponer detalles de infraestructura.
 */
public class CustomerServiceUnavailableException extends RuntimeException {

    public CustomerServiceUnavailableException(String message) {
        super(message);
    }
}
