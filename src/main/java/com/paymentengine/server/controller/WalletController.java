package com.paymentengine.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.service.WalletService;
import com.paymentengine.server.service.TransactionService;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // Create wallet when user is logged
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Wallet createWallet(@RequestParam String nameWallet, @RequestParam BigDecimal balance,  
                               @RequestParam String currency, HttpSession session) {
      
                                               
       Integer userId = getLoggedUserId(session);

       return walletService.createWallet(userId, nameWallet, balance, currency);
       
       //return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }
    
    // GET MY WALLETS
    @GetMapping("/me")
    public ResponseEntity<List<Wallet>> getMyWallets(HttpSession session) {

        Integer userId = getLoggedUserId(session);

        List<Wallet> wallets = walletService.getAllWalletsByUserId(userId);

        return ResponseEntity.ok(wallets);
    }

   
    // GET WALLET BY ID (ownership check)
    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable int walletId, HttpSession session) {

        Integer userId = getLoggedUserId(session);

        Wallet wallet = walletService.getWalletById(walletId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found"));

        if (wallet.getUserId() != userId) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok(wallet);
    }

   
    // Check if wallet has sufficient funds (ownership check)
    @GetMapping("/{walletId}/has-funds")
    public ResponseEntity<Boolean> hasFunds(@PathVariable int walletId, @RequestParam BigDecimal amount, HttpSession session) {

        Integer userId = getLoggedUserId(session);

        Wallet wallet = walletService.getWalletById(walletId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found"));

        if (wallet.getUserId() != userId) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok(
                walletService.hasSufficientFunds(walletId, amount)
        );
    }

    @DeleteMapping("/{walletId}")
        public ResponseEntity<Void> deleteWallet(@PathVariable int walletId,
                                            HttpSession session) {

        Integer userId = getLoggedUserId(session);

        walletService.deleteWallet(walletId, userId);

        return ResponseEntity.noContent().build();
    }

    private Integer getLoggedUserId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not logged in"
            );
        }

        return userId;
    }
}