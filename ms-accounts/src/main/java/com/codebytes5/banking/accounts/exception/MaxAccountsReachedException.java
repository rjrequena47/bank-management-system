package com.codebytes5.banking.accounts.exception;

public class MaxAccountsReachedException extends RuntimeException {

    public MaxAccountsReachedException() {
        super("Ha alcanzado el límite máximo de cuentas permitidas (3).");
    }
}
