package com.paymentengine.server.controller;

import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.service.TransactionService;
import com.paymentengine.server.service.WalletService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/wallets/{walletId}/transactions")
public class TransactionController {

   private final TransactionService transactionService;
   private final WalletService walletService;

    public TransactionController(TransactionService transactionService, WalletService walletService) {
        this.transactionService = transactionService;
        this.walletService = walletService;
    }

    private int getLoggedUserId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }
        return userId;
    }

    // Get all transactions for a wallet
    @GetMapping
    public List<Transaction> getTransactionsByWallet(@PathVariable int walletId, HttpSession session) {
        int loggedUserId = getLoggedUserId(session);

        // check wallet ownership
        Wallet wallet = walletService.getWalletById(walletId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Wallet not found"));

        if (wallet.getUserId() != loggedUserId) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return transactionService.viewWalletTransactions(walletId);
    }

    @GetMapping("/details")
    public List<Transaction> getTransactionsWithDetails(@PathVariable int walletId, HttpSession session) {

        int loggedUserId = getLoggedUserId(session);

        Wallet wallet = walletService.getWalletById(walletId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found"));

        if (wallet.getUserId() != loggedUserId) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return transactionService.getTransactionsWithCounterparty(walletId);
    }

}

