package com.wallet.wallet.controller;

import com.wallet.wallet.dto.ConsumeRequest;
import com.wallet.wallet.dto.TopupRequest;
import com.wallet.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/**
 * REST Controller for all wallet-related operations.
 */
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/{walletId}/topup")
    public ResponseEntity<String> topupWallet(@PathVariable Long walletId, @Valid @RequestBody TopupRequest request) {
        walletService.topup(walletId, request.getAmount(), request.getTransactionId());
        return ResponseEntity.ok("Top-up successful");
    }

    @PostMapping("/{walletId}/consume")
    public ResponseEntity<String> consume(@PathVariable Long walletId, @Valid @RequestBody ConsumeRequest request) {
        walletService.consume(walletId, request.getAmount(), request.getTransactionId());
        return ResponseEntity.ok("Consumption successful");
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long walletId) {
        BigDecimal balance = walletService.getBalance(walletId);
        return ResponseEntity.ok(balance);
    }
}