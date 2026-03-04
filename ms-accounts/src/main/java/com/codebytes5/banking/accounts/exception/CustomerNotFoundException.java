package com.codebytes5.banking.accounts.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    /**
     * Constructor utilizado desde los servicios de dominio cuando se conoce el UUID
     * del cliente.
     * El UUID NO se incluye en el mensaje para evitar exposición de datos internos
     * al cliente.
     */
    public CustomerNotFoundException(UUID customerId) {
        super("No se encontró el cliente asociado a esta operación.");
    }

    /**
     * Constructor utilizado desde
     * {@link com.codebytes5.banking.accounts.client.CustomerClientErrorDecoder}
     * cuando el error proviene de ms-customers y no se dispone del UUID.
     */
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
