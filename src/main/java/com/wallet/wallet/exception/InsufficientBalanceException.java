package com.wallet.wallet.exception;

/**
 * Exception thrown when a consume operation is attempted with insufficient funds.
 * Results in an HTTP 400 Bad Request response (Insuficient funds).
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}