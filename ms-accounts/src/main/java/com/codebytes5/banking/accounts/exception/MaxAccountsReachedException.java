package com.codebytes5.banking.accounts.exception;

public class MaxAccountsReachedException extends RuntimeException {

    public MaxAccountsReachedException() {
        super("Customer has reached the maximum number of accounts (3)");
    }
}
