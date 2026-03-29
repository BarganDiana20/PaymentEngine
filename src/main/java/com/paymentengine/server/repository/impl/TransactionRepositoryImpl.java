package com.paymentengine.server.repository.impl;

import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.model.TransactionStatus;
import com.paymentengine.server.repository.TransactionRepository;
import com.paymentengine.server.repository.session.DatabaseConnection;

import org.hibernate.validator.constraints.pl.REGON;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {
    
    private final Jdbi jdbi;
 
    public TransactionRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    
    @Override
    public Optional<Transaction> findById(int id) {

        return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM transactions WHERE id = :id")
                .bind("id", id)
                .mapToBean(Transaction.class)
                .findFirst()
        );
    }


    @Override
    public Transaction insert(Transaction transaction) {

        int generatedId = jdbi.withHandle(
            handle -> handle.createUpdate("INSERT INTO transactions (wallet_id, payment_id, type, amount, currency, description, status, created_at)VALUES (:walletId, :paymentId, :type, :amount, :currency, :description, :status, :createdAt)")
            .bind("walletId", transaction.getWalletId())
            .bind("paymentId", transaction.getPaymentId())
            .bind("type", transaction.getType().name())
            .bind("amount", transaction.getAmount())
            .bind("currency", transaction.getCurrency())
            .bind("description", transaction.getDescription())
            .bind("status", transaction.getStatus().name())
            .bind("createdAt", transaction.getCreatedAt())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(int.class)
            .one()
        );

        transaction.setId(generatedId);
        return transaction;
    }

    @Override
    public List<Transaction> findByUserId(Integer userId) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                SELECT t.*
                FROM transactions t
                JOIN wallets w ON t.wallet_id = w.id
                WHERE w.user_id = :userId
                ORDER BY t.created_at DESC
            """)
            .bind("userId", userId)
            .mapToBean(Transaction.class)
            .list()
        );
    }

    @Override
    public List<Transaction> findByWalletId(int walletId) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                SELECT * FROM transactions
                WHERE wallet_id = :walletId
                ORDER BY created_at DESC
            """)
            .bind("walletId", walletId)
            .mapToBean(Transaction.class)
            .list()
        );
    }

    @Override
    public List<Transaction> findAllByWalletIdWithCounterparty(int walletId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT 
                    t.*,
                    u.first_name AS counterpartyFirstName,
                    u.last_name AS counterpartyLastName
                FROM transactions t
                JOIN payments p ON t.payment_id = p.id
                JOIN wallets w ON
                    (
                        (t.type = 'DEBIT' AND w.code_wallet = p.to_wallet_code)
                        OR
                        (t.type = 'CREDIT' AND w.id = p.wallet_id)
                    )
                JOIN users u ON u.id = w.user_id
                WHERE t.wallet_id = :walletId
                ORDER BY t.created_at DESC
            """)
            .bind("walletId", walletId)
            .mapToBean(Transaction.class)
            .list()
        );
    }

    @Override
    public List<Transaction> findByDateRange( int walletId, LocalDateTime from, LocalDateTime to) {

        return jdbi.withHandle(handle -> handle.createQuery("""
                SELECT * FROM transactions
                WHERE wallet_id = :walletId
                AND created_at BETWEEN :from AND :to
                ORDER BY created_at DESC
            """)
            .bind("walletId", walletId)
            .bind("from", from)
            .bind("to", to)
            .mapToBean(Transaction.class)
            .list()
        );
    }

    @Override
    public void updateStatus(int transactionId, TransactionStatus status) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
                UPDATE transactions
                SET status = :status
                WHERE id = :id
            """)
            .bind("status", status.name())
            .bind("id", transactionId)
            .execute()
        );
    }

    @Override
    public void updateStatusByPaymentId(int paymentId, TransactionStatus status) {
        jdbi.useHandle(handle ->
            handle.createUpdate("UPDATE transactions SET status = :status WHERE payment_id = :paymentId")
                .bind("status", status.name())
                .bind("paymentId", paymentId)
                .execute()
        );
    }

}
