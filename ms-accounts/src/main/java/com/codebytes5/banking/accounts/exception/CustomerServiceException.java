package com.codebytes5.banking.accounts.exception;

/**
 * Excepción lanzada cuando ms-customers retorna un error no esperado (ej. HTTP
 * 500).
 * Se mapea a HTTP 502 Bad Gateway para comunicar al cliente un fallo en
 * servicio externo
 * sin exponer detalles internos.
 */
public class CustomerServiceException extends RuntimeException {

    public CustomerServiceException(String message) {
        super(message);
    }
}
