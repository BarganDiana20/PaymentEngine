package com.paymentengine.server.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.paymentengine.server.model.Payment;
import com.paymentengine.server.model.PaymentStatus;
import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.service.PaymentService;
import com.paymentengine.server.service.WalletService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/wallets/{walletId}/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final WalletService walletService;

    public PaymentController(PaymentService paymentService, WalletService walletService) {
        this.paymentService = paymentService;  
        this.walletService = walletService;  
    }

    private int getLoggedUserId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("Not logged in! ");
        }
        return userId;
    }
    
    //Create payment
    @PostMapping
    public Payment createPayment(
            @PathVariable int walletId, @RequestParam BigDecimal amount,
            @RequestParam String description, HttpSession session) {
        
        getLoggedUserId(session); //check if user is logged

        return paymentService.processPayment(walletId, amount, description);
    }

    @PostMapping("/transfer")
    public Payment transfer(
        @PathVariable int walletId, @RequestParam int toWalletCode, @RequestParam BigDecimal amount, 
        @RequestParam(required = false) String description, HttpSession session) {

        int loggedUserId = getLoggedUserId(session);

        //we check if the source wallet belongs to the logged in user
        Wallet fromWallet = walletService.getWalletById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found."));
        
        if (fromWallet.getUserId() != loggedUserId) {
            throw new SecurityException("You can only transfer from your own wallet.");
        }

        String currency = fromWallet.getCurrency();

        //check if the destination wallet exists
        Wallet toWallet = walletService.getWalletByCode(toWalletCode) 
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found."));
        
        return paymentService.transfer(walletId, toWalletCode, amount, description);
    }

    // Create scheduled payment
    @PostMapping("/scheduled")
    public Payment createScheduledPayment(
            @PathVariable int walletId, @RequestParam int toWalletCode, @RequestParam BigDecimal amount,
            @RequestParam String scheduledAt, @RequestParam(required = false) String description, 
            HttpSession session) {

        int loggedUserId = getLoggedUserId(session);
    
        Wallet fromWallet = walletService.getWalletById(walletId)
            .orElseThrow(() -> new IllegalArgumentException("Source wallet not found."));

        if (fromWallet.getUserId() != loggedUserId) {
            throw new SecurityException("You can only transfer from your own wallet.");
        }

        String currency = fromWallet.getCurrency();
        
        Wallet toWallet = walletService.getWalletByCode(toWalletCode)
            .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found."));

        Payment payment = new Payment();
        payment.setWalletId(walletId);
        payment.setToWalletCode(toWalletCode);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setScheduledAt(LocalDateTime.parse(scheduledAt));
        payment.setCreatedAt(LocalDateTime.now());

        return paymentService.createScheduledPayment(payment, description);
    }

    // Put cancel payment
    @PutMapping("/{paymentId}/cancel")
    public void cancelPayment(@PathVariable int walletId, @PathVariable int paymentId, HttpSession session) {
        
        getLoggedUserId(session); //check if user is logged

        Payment payment = paymentService.getPaymentById(paymentId);

        if (payment.getWalletId() != walletId) {
            throw new SecurityException("Payment does not belong to this wallet");
        }

        paymentService.cancelPayment(paymentId);
    }

    // Get payment by id
    @GetMapping("/{paymentId:\\d+}")
    public Payment getPayment(@PathVariable("paymentId") int paymentId, HttpSession session) {
       
        getLoggedUserId(session); 
        return paymentService.getPaymentById(paymentId);
    }

    // Get all payments for a wallet
    // url: get /wallets/walletId/payments
    @GetMapping
    public List<Payment> getPaymentsByWallet(@PathVariable int walletId, HttpSession session) {
       
        getLoggedUserId(session); 
        return paymentService.getPaymentsByWalletId(walletId);
    }

}
