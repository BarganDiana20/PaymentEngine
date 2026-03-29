package com.paymentengine.server.controller;

import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminTransactionController {

    private final TransactionService transactionService;

    public AdminTransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    private void checkAdmin(HttpSession session) {

        Object role = session.getAttribute("role");

        if (role == null || !role.toString().equals("ADMIN")) {
            throw new SecurityException("Access denied. Admin only.");
        }
    }

    // GET /admin/users/username/{username}/transactions
    @GetMapping("/users/username/{username}/transactions")
    public List<Transaction> getTransactionsByUsername(
            @PathVariable String username,
            HttpSession session) {

        checkAdmin(session);
        return transactionService.getTransactionsByUsername(username);
    }

    // GET /admin/wallets/code/{walletCode}/transactions
    @GetMapping("/wallets/code/{walletCode}/transactions")
    public List<Transaction> getTransactionsByWalletCode(
            @PathVariable int walletCode,
            HttpSession session) {

        checkAdmin(session);
        return transactionService.getTransactionsByWalletCode(walletCode);
    }
}


