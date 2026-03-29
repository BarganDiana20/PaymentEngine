package com.paymentengine.server.service;

import com.paymentengine.server.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    
    //operation for user
    List<Transaction> viewWalletTransactions(int walletId);
   
    //operation for admin
    List<Transaction> getTransactionsByUsername(String username);

    List<Transaction> getTransactionsByWalletCode(int walletCode);

    List<Transaction> getTransactionsWithCounterparty(int walletId);
}
