package com.codebytes5.banking.accounts.exception;

public class IbanGenerationException extends RuntimeException {

    public IbanGenerationException() {
        super("No fue posible generar un número de cuenta único. Intente nuevamente.");
    }
}
