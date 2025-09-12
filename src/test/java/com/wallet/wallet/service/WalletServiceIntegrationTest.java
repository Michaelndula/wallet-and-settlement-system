package com.wallet.wallet.service;

import com.wallet.wallet.IntegrationTestBase;
import com.wallet.wallet.exception.InsufficientBalanceException;
import com.wallet.wallet.exception.TransactionAlreadyExistsException;
import com.wallet.wallet.model.Wallet;
import com.wallet.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the WalletService.
 * This test class interacts with a real database and message queue
 * started by Testcontainers via the IntegrationTestBase class.
 */
public class WalletServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void testTopupAndConsume_SuccessfulFlow() {
        // Arrange
        Long walletId = 1L;
        BigDecimal topupAmount = new BigDecimal("100.00");
        BigDecimal consumeAmount = new BigDecimal("40.50");
        BigDecimal expectedFinalBalance = new BigDecimal("59.50");

        // Top up a new wallet
        walletService.topup(walletId, topupAmount, "txn-integ-1");

        // Check if the balance is correct in the database
        Optional<Wallet> walletAfterTopup = walletRepository.findById(walletId);
        assertTrue(walletAfterTopup.isPresent(), "Wallet should be created after topup.");
        assertEquals(0, topupAmount.compareTo(walletAfterTopup.get().getBalance()), "Balance after topup should be correct.");

        // Consume from the wallet
        walletService.consume(walletId, consumeAmount, "txn-integ-2");

        // Check the final balance
        Optional<Wallet> walletAfterConsume = walletRepository.findById(walletId);
        assertTrue(walletAfterConsume.isPresent());
        assertEquals(0, expectedFinalBalance.compareTo(walletAfterConsume.get().getBalance()), "Final balance after consume should be correct.");
    }

    @Test
    void testConsume_shouldThrowInsufficientBalanceException() {
        // Arrange
        Long walletId = 2L;
        walletService.topup(walletId, new BigDecimal("20.00"), "txn-integ-3");

        // Act & Assert
        // Verify that trying to consume more than the balance throws the correct exception
        assertThrows(InsufficientBalanceException.class, () -> {
            walletService.consume(walletId, new BigDecimal("50.00"), "txn-integ-4");
        });
    }

    @Test
    void testTopup_shouldThrowDuplicateTransactionException() {
        // Arrange
        Long walletId = 3L;
        String duplicateTransactionId = "txn-integ-duplicate";
        walletService.topup(walletId, new BigDecimal("10.00"), duplicateTransactionId);

        // Act & Assert
        // Verify that using the same transaction ID again throws the correct exception
        assertThrows(TransactionAlreadyExistsException.class, () -> {
            walletService.topup(walletId, new BigDecimal("10.00"), duplicateTransactionId);
        });
    }
}
