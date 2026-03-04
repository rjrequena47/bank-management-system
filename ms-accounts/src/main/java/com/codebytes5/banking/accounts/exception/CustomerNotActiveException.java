package com.codebytes5.banking.accounts.exception;

import java.util.UUID;

public class CustomerNotActiveException extends RuntimeException {

    /**
     * Constructor utilizado desde los servicios de dominio cuando se conoce el UUID
     * del cliente.
     * El UUID NO se incluye en el mensaje para evitar exposición de datos internos
     * al cliente.
     */
    public CustomerNotActiveException(UUID customerId) {
        super("El cliente no está activo. Por favor contacte con soporte.");
    }

    /**
     * Constructor utilizado desde
     * {@link com.codebytes5.banking.accounts.client.CustomerClientErrorDecoder}
     * cuando el error proviene de ms-customers y no se dispone del UUID.
     */
    public CustomerNotActiveException(String message) {
        super(message);
    }
}
