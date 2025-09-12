package com.wallet.wallet.exception;

/**
 * Exception thrown when a wallet is not found for a given ID.
 * Results in an HTTP 404 Not Found response.
 */
public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}