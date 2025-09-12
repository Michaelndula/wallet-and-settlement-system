package com.wallet.wallet.controller;

import com.wallet.wallet.dto.ReconciliationReport;
import com.wallet.wallet.service.ReconciliationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @GetMapping("/report")
    public ResponseEntity<ReconciliationReport> getReconciliationReport(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ReconciliationReport report = reconciliationService.generateReport(date);
        return ResponseEntity.ok(report);
    }

    /**
     * Endpoint to export the reconciliation report as a CSV file.
     * @param date The date for which to generate the report.
     * @param response The HTTP response object, used to write the file content.
     * @throws IOException If an error occurs while writing the file.
     */
    @GetMapping("/report/csv")
    public void exportReconciliationReportAsCsv(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"reconciliation_report_" + date + ".csv\"");

        reconciliationService.writeReconciliationReportToCsv(date, response.getWriter());
    }
}