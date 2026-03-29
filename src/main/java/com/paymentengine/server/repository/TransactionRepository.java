package com.paymentengine.server.repository;

import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.model.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    
    Transaction insert(Transaction transaction);
    
    Optional<Transaction> findById(int id);

    // view historical transactions by the user
    List<Transaction> findByWalletId(int walletId);

    List<Transaction> findAllByWalletIdWithCounterparty(int walletId);

    //view  historical transactions by the admin / backoffice
    List<Transaction> findByUserId(Integer userId);

    List<Transaction> findByDateRange(int walletId, LocalDateTime from, LocalDateTime to);
    
    //only for updating transactions by id from pending status to completed
    void updateStatus(int transactionId, TransactionStatus status);

    void updateStatusByPaymentId(int paymentId, TransactionStatus status);
}
