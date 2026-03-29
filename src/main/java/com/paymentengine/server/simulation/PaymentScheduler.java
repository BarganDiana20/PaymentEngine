package com.paymentengine.server.simulation;

import com.google.inject.Inject;

import static org.mockito.Mockito.description;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentengine.server.model.Payment;
import com.paymentengine.server.model.PaymentStatus;
import com.paymentengine.server.repository.PaymentRepository;
import com.paymentengine.server.service.PaymentService;

@Component
public class PaymentScheduler {
   
    //dependencies:
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private LocalDateTime lastRun;

    public PaymentScheduler(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
            this::processPendingPayments, 
            0, 5, TimeUnit.SECONDS
        );
    }

    private void processPendingPayments() {
        lastRun = LocalDateTime.now();

        // we take over all PENDING payments
        List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

        for (Payment payment : pendingPayments) {
            try {
                LocalDateTime now = LocalDateTime.now();

                if (payment.getScheduledAt() != null &&
                    payment.getScheduledAt().isAfter(now)){
                    continue; // it's not time yet
                }

                paymentService.processScheduledPayment(payment);

            } catch (IllegalStateException e) {
                 //if funds are insufficient, we mark the payment as failed
                paymentRepository.updateStatus(payment.getId(), PaymentStatus.FAILED);
            } catch (Exception e) {
                // here we can do logging, retry, alert etc.
            }
        }
    }

    public LocalDateTime getScheduledAt() {
        return lastRun;
    }

    public void shutdown() {
        scheduler.shutdown();
            try {
                   if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                   }
                } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                        }
        }
}
