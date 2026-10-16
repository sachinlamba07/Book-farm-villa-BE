package com.bookfarmvilla.exception;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Account is locked for security purposes. Contact support to unlock your account.");
    }
}
