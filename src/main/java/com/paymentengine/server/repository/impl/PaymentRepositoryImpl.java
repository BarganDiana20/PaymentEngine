package com.paymentengine.server.repository.impl;

import org.springframework.stereotype.Repository;
import com.paymentengine.server.model.Payment;
import com.paymentengine.server.model.PaymentStatus;
import com.paymentengine.server.repository.PaymentRepository;
import com.paymentengine.server.repository.session.DatabaseConnection;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    
    private final Jdbi jdbi;

    public PaymentRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Payment insert(Payment payment){
        int generatedId = jdbi.withHandle(
            handle -> handle.createUpdate("""
                        INSERT INTO payments 
                        (wallet_id, to_wallet_code, amount, currency, status, scheduled_at, created_at)
                          VALUES 
                        (:walletId, :toWalletCode, :amount, :currency, :status, :scheduledAt, :createdAt)
                    """)
            .bind("walletId", payment.getWalletId())
            .bind("toWalletCode", payment.getToWalletCode())
            .bind("amount", payment.getAmount())
            .bind("currency", payment.getCurrency())
            .bind("status", payment.getStatus().name())
            .bind("scheduledAt", payment.getScheduledAt())
            .bind("createdAt", payment.getCreatedAt())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(int.class)
            .one()
        );

        payment.setId(generatedId);
        return payment;
    }

    @Override
    public Optional<Payment> findById(int id){
        return jdbi.withHandle(
            handle -> handle.createQuery("SELECT * FROM payments WHERE id = :id")
                  .bind("id", id)
                  .mapToBean(Payment.class)
                  .findFirst()
                );
    }

    @Override
    public List<Payment> findByWalletId(int walletId){
        return jdbi.withHandle(
            handle -> handle.createQuery("SELECT * FROM payments WHERE wallet_id = :walletId")
                  .bind("walletId", walletId)
                  .mapToBean(Payment.class)
                  .list()
                );
    }
   
    @Override
    public List<Payment> findByStatus(PaymentStatus status){
        return jdbi.withHandle(handle ->
             handle.createQuery("SELECT * FROM payments WHERE status = :status")
              .bind("status", status.name())
              .mapToBean(Payment.class)
              .list()
        );
    }

    @Override
    public void updateStatus(int paymentId, PaymentStatus status) {
        jdbi.useHandle(handle ->
            handle.createUpdate("UPDATE payments SET status = :status WHERE id = :id")
                  .bind("status", status.name())
                  .bind("id", paymentId)
                  .execute()
        );
    }


}
