package com.wallet.wallet.dto;

import com.wallet.wallet.model.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A simple, JSON-friendly representation of a Transaction.
 * This decouples the API response from the database entity.
 */
@Data
@Builder
public class TransactionDTO {
    private String transactionId;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime createdAt;
    private Long walletId;
}