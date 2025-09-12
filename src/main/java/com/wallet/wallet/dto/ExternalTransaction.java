package com.wallet.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for representing a transaction from an external report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTransaction {
    private String transactionId;
    private BigDecimal amount;
}