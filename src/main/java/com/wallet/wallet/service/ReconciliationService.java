package com.wallet.wallet.service;

import com.wallet.wallet.dto.ExternalTransaction;
import com.wallet.wallet.dto.ReconciliationReport;
import com.wallet.wallet.dto.TransactionDTO;
import com.wallet.wallet.model.Transaction;
import com.wallet.wallet.repository.TransactionRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationService {

    private final TransactionRepository transactionRepository;

    public ReconciliationReport generateReport(LocalDate date) {
        log.info("Generating reconciliation report for date: {}", date);

        List<ExternalTransaction> externalTransactions = readExternalTransactions(date);
        List<Transaction> internalTransactions = transactionRepository.findByCreatedAtBetween(
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );

        // Convert internal transactions to DTOs for clean processing
        List<TransactionDTO> internalTransactionDTOs = internalTransactions.stream()
                .map(this::toTransactionDTO)
                .collect(Collectors.toList());

        Map<String, TransactionDTO> internalMap = internalTransactionDTOs.stream()
                .collect(Collectors.toMap(TransactionDTO::getTransactionId, Function.identity()));

        Map<String, ExternalTransaction> externalMap = externalTransactions.stream()
                .collect(Collectors.toMap(ExternalTransaction::getTransactionId, Function.identity()));

        List<TransactionDTO> matched = new ArrayList<>();
        Map<String, ReconciliationReport.MismatchDetail> mismatched = new java.util.HashMap<>();
        List<TransactionDTO> missingInExternal = new ArrayList<>();
        List<ExternalTransaction> missingInInternal = new ArrayList<>();

        internalMap.forEach((id, internalTx) -> {
            if (externalMap.containsKey(id)) {
                ExternalTransaction externalTx = externalMap.get(id);
                if (internalTx.getAmount().compareTo(externalTx.getAmount()) == 0) {
                    matched.add(internalTx);
                } else {
                    mismatched.put(id, ReconciliationReport.MismatchDetail.builder()
                            .internalAmount(internalTx.getAmount())
                            .externalAmount(externalTx.getAmount())
                            .build());
                }
            } else {
                missingInExternal.add(internalTx);
            }
        });

        externalMap.forEach((id, externalTx) -> {
            if (!internalMap.containsKey(id)) {
                missingInInternal.add(externalTx);
            }
        });

        return ReconciliationReport.builder()
                .reportDate(date.toString())
                .totalInternalTransactions(internalTransactions.size())
                .totalExternalTransactions(externalTransactions.size())
                .matchedCount(matched.size())
                .mismatchedCount(mismatched.size())
                .missingInExternalCount(missingInExternal.size())
                .missingInInternalCount(missingInInternal.size())
                .matched(matched)
                .mismatched(mismatched)
                .missingInExternal(missingInExternal)
                .missingInInternal(missingInInternal)
                .build();
    }

    /**
     * Generates a reconciliation report and writes it directly to a CSV stream.
     * @param date The date for the report.
     * @param writer The PrintWriter to write the CSV content to.
     * @throws IOException if an I/O error occurs
     */
    public void writeReconciliationReportToCsv(LocalDate date, PrintWriter writer) throws IOException {
        ReconciliationReport report = generateReport(date);

        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            // --- Summary Section ---
            csvWriter.writeNext(new String[]{"Reconciliation Summary Report"});
            csvWriter.writeNext(new String[]{"Report Date:", report.getReportDate()});
            csvWriter.writeNext(new String[]{});
            csvWriter.writeNext(new String[]{"Total Internal Transactions:", String.valueOf(report.getTotalInternalTransactions())});
            csvWriter.writeNext(new String[]{"Total External Transactions:", String.valueOf(report.getTotalExternalTransactions())});
            csvWriter.writeNext(new String[]{"Matched:", String.valueOf(report.getMatchedCount())});
            csvWriter.writeNext(new String[]{"Mismatched:", String.valueOf(report.getMismatchedCount())});
            csvWriter.writeNext(new String[]{"Missing in External:", String.valueOf(report.getMissingInExternalCount())});
            csvWriter.writeNext(new String[]{"Missing in Internal:", String.valueOf(report.getMissingInInternalCount())});
            csvWriter.writeNext(new String[]{});

            // --- Matched Transactions ---
            csvWriter.writeNext(new String[]{"Matched Transactions"});
            csvWriter.writeNext(new String[]{"Transaction ID", "Amount", "Type", "Timestamp"});
            for (TransactionDTO tx : report.getMatched()) {
                csvWriter.writeNext(new String[]{tx.getTransactionId(), tx.getAmount().toString(), tx.getType().toString(), tx.getCreatedAt().toString()});
            }
            csvWriter.writeNext(new String[]{});

            // --- Mismatched Transactions ---
            csvWriter.writeNext(new String[]{"Mismatched Transactions"});
            csvWriter.writeNext(new String[]{"Transaction ID", "Internal Amount", "External Amount"});
            report.getMismatched().forEach((id, detail) -> csvWriter.writeNext(new String[]{id, detail.getInternalAmount().toString(), detail.getExternalAmount().toString()}));
            csvWriter.writeNext(new String[]{});

            // --- Missing in External Report (Internal Only) ---
            csvWriter.writeNext(new String[]{"Missing in External Report (Internal Only)"});
            csvWriter.writeNext(new String[]{"Transaction ID", "Amount", "Type", "Timestamp"});
            for (TransactionDTO tx : report.getMissingInExternal()) {
                csvWriter.writeNext(new String[]{tx.getTransactionId(), tx.getAmount().toString(), tx.getType().toString(), tx.getCreatedAt().toString()});
            }
            csvWriter.writeNext(new String[]{});

            // --- Missing in Internal Report (External Only) ---
            csvWriter.writeNext(new String[]{"Missing in Internal Report (External Only)"});
            csvWriter.writeNext(new String[]{"Transaction ID", "Amount"});
            for (ExternalTransaction tx : report.getMissingInInternal()) {
                csvWriter.writeNext(new String[]{tx.getTransactionId(), tx.getAmount().toString()});
            }
        }
    }


    private List<ExternalTransaction> readExternalTransactions(LocalDate date) {
        List<ExternalTransaction> transactions = new ArrayList<>();
        String fileName = "external_transactions_" + date + ".csv";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
             com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new InputStreamReader(is))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                transactions.add(new ExternalTransaction(line[0], new BigDecimal(line[1])));
            }
        } catch (IOException | CsvValidationException | NullPointerException e) {
            log.error("Error reading external transaction file: {}", fileName, e);
            // Return empty list if file not found or error occurs
        }
        return transactions;
    }

    /**
     * Converts a Transaction entity to a TransactionDTO.
     * This is the key to preventing the JSON serialization error.
     */
    private TransactionDTO toTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .createdAt(transaction.getCreatedAt())
                .walletId(transaction.getWallet().getId())
                .build();
    }
}

