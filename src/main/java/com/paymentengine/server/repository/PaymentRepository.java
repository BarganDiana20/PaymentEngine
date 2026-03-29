package com.paymentengine.server.repository;

import com.paymentengine.server.model.Payment;
import com.paymentengine.server.model.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    
    Payment insert(Payment payment);

    Optional<Payment> findById(int id);

    List<Payment> findByWalletId(int walletId);

    List<Payment> findByStatus(PaymentStatus status);

    void updateStatus(int paymentId, PaymentStatus status); 
  
}
