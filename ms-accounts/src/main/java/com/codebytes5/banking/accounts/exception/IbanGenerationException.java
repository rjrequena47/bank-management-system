package com.codebytes5.banking.accounts.exception;

public class IbanGenerationException extends RuntimeException {

    public IbanGenerationException() {
        super("Unable to generate a unique IBAN after 5 attempts");
    }
}
