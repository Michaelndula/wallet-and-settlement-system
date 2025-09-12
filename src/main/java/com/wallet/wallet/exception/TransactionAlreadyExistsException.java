package com.wallet.wallet.exception;

/**
 * Exception for handling duplicate transaction IDs to ensure idempotency.
 * Results in an HTTP 409 Conflict response (return error Duplicate transactio).
 */
public class TransactionAlreadyExistsException extends RuntimeException {
    public TransactionAlreadyExistsException(String message) {
        super(message);
    }
}