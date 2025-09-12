package com.wallet.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for handling service consumption requests.
 * Structurally similar to TopupRequest but used for a different business action.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeRequest {
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Transaction ID cannot be blank")
    private String transactionId;
}
