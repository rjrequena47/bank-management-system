package com.codebytes5.banking.accounts.exception;

public class DailyWithdrawalLimitExceededException extends RuntimeException {
    public DailyWithdrawalLimitExceededException(String message) {
        super(message);
    }
}
