package com.codebytes5.banking.accounts.exception;

import java.util.UUID;

public class CustomerNotActiveException extends RuntimeException {

    public CustomerNotActiveException(UUID customerId) {
        super("Customer is not active: " + customerId);
    }
}
