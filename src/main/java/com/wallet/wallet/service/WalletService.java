package com.wallet.wallet.service;

import com.wallet.wallet.config.RabbitMQConfig;
import com.wallet.wallet.dto.ConsumeRequest;
import com.wallet.wallet.dto.TopupRequest;
import com.wallet.wallet.exception.InsufficientBalanceException;
import com.wallet.wallet.exception.TransactionAlreadyExistsException;
import com.wallet.wallet.exception.WalletNotFoundException;
import com.wallet.wallet.model.Transaction;
import com.wallet.wallet.model.TransactionType;
import com.wallet.wallet.model.Wallet;
import com.wallet.wallet.repository.TransactionRepository;
import com.wallet.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void topup(Long walletId, BigDecimal amount, String transactionId) {
        transactionRepository.findByTransactionId(transactionId).ifPresent(t -> {
            throw new TransactionAlreadyExistsException("Transaction with ID " + transactionId + " already exists.");
        });

        Wallet wallet = walletRepository.findById(walletId)
                .orElseGet(() -> createNewWallet(walletId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        createAndSaveTransaction(wallet, amount, transactionId, TransactionType.TOPUP);

        log.info("Wallet {} topped up by {}", walletId, amount);
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, new TopupRequest(amount, transactionId));
    }

    @Transactional
    public void consume(Long walletId, BigDecimal amount, String transactionId) {
        transactionRepository.findByTransactionId(transactionId).ifPresent(t -> {
            throw new TransactionAlreadyExistsException("Transaction with ID " + transactionId + " already exists.");
        });

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with ID " + walletId + " not found."));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for wallet ID " + walletId);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        createAndSaveTransaction(wallet, amount, transactionId, TransactionType.CONSUME);

        log.info("Wallet {} consumed by {}", walletId, amount);
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, new ConsumeRequest(amount, transactionId));
    }

    public BigDecimal getBalance(Long walletId) {
        return walletRepository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with ID " + walletId + " not found."));
    }

    private Wallet createNewWallet(Long walletId) {
        Wallet newWallet = new Wallet();
        newWallet.setId(walletId);
        newWallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(newWallet);
    }

    private void createAndSaveTransaction(Wallet wallet, BigDecimal amount, String transactionId, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setTransactionId(transactionId);
        transaction.setType(type);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}