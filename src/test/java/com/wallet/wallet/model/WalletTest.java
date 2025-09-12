package com.wallet.wallet.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the Wallet entity.
 * These tests verify the correct instantiation and property handling of the Wallet class.
 */
class WalletTest {

    @Test
    void testWalletCreationAndProperties() {
        // test data
        Long walletId = 12345L;
        BigDecimal initialBalance = new BigDecimal("150.75");

        // Create a new Wallet instance and set its properties
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(initialBalance);

        // Verify that the properties were set correctly
        assertNotNull(wallet, "The wallet object should not be null.");
        assertEquals(walletId, wallet.getId(), "The wallet ID should match the set value.");

        // Use compareTo for BigDecimal equality checks for accuracy
        assertEquals(0, initialBalance.compareTo(wallet.getBalance()), "The balance should match the set value.");
    }

    @Test
    void testDefaultBalanceIsZero() {
        // Arrange & Act: Create a new wallet without setting a balance
        Wallet wallet = new Wallet();
        wallet.setId(999L);

        // Assert: Verify that the default balance is BigDecimal.ZERO
        assertNotNull(wallet.getBalance(), "Balance should not be null.");
        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getBalance()), "The default balance should be zero.");
    }
}