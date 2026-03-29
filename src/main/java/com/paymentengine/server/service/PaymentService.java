package com.paymentengine.server.service;
import java.math.BigDecimal;
import java.util.List;

import com.paymentengine.server.model.Payment;

public interface PaymentService {

    Payment processPayment(int walletId, BigDecimal amount, String description);
   
    Payment transfer(int fromWalletId, int toWalletCode, BigDecimal amount, String description);
   
    Payment createScheduledPayment(Payment payment, String description);
    
    Payment processScheduledPayment(Payment payment);

    void cancelPayment(int paymentId);
    
    Payment getPaymentById(int paymentId);

    List<Payment> getPaymentsByWalletId(int walletId);


}
