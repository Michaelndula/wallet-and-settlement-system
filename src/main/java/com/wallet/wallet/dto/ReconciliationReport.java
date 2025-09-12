package com.wallet.wallet.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * DTO for the reconciliation report.
 * Uses TransactionDTO to ensure clean JSON serialization.
 */
@Data
@Builder
public class ReconciliationReport {
    private String reportDate;
    private int totalInternalTransactions;
    private int totalExternalTransactions;
    private int matchedCount;
    private int mismatchedCount;
    private int missingInExternalCount;
    private int missingInInternalCount;

    private List<TransactionDTO> matched;
    private Map<String, MismatchDetail> mismatched;
    private List<TransactionDTO> missingInExternal;
    private List<ExternalTransaction> missingInInternal;

    @Data
    @Builder
    public static class MismatchDetail {
        private java.math.BigDecimal internalAmount;
        private java.math.BigDecimal externalAmount;
    }
}