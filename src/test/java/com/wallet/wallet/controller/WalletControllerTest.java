package com.wallet.wallet.controller;

import com.wallet.wallet.dto.ConsumeRequest;
import com.wallet.wallet.dto.TopupRequest;
import com.wallet.wallet.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the WalletController.
 * Uses MockMvc to simulate HTTP requests and tests the controller in isolation.
 */
@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    @Test
    void topupWallet_shouldReturnSuccess() throws Exception {
        // Arrange
        Long walletId = 123L;
        TopupRequest topupRequest = new TopupRequest(new BigDecimal("100.00"), "txn-topup-1");

        // Mock the service call, as we are not testing the service logic here
        doNothing().when(walletService).topup(walletId, topupRequest.getAmount(), topupRequest.getTransactionId());

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallets/{walletId}/topup", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Top-up successful"));
    }

    @Test
    void consume_shouldReturnSuccess() throws Exception {
        // Arrange
        Long walletId = 123L;
        ConsumeRequest consumeRequest = new ConsumeRequest(new BigDecimal("50.00"), "txn-consume-1");

        // Mock the service call
        doNothing().when(walletService).consume(walletId, consumeRequest.getAmount(), consumeRequest.getTransactionId());

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallets/{walletId}/consume", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consumeRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Consumption successful"));
    }

    @Test
    void getBalance_shouldReturnBalance() throws Exception {
        // Arrange
        Long walletId = 123L;
        BigDecimal expectedBalance = new BigDecimal("250.50");

        // Mock the service call to return a specific balance
        when(walletService.getBalance(walletId)).thenReturn(expectedBalance);

        // Act & Assert
        mockMvc.perform(get("/api/v1/wallets/{walletId}/balance", walletId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedBalance)));
    }
}
