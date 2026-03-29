package com.paymentengine;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.paymentengine.server.model.Role;
import com.paymentengine.server.model.User;
import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.model.Payment;
import com.paymentengine.server.service.PaymentService;
import com.paymentengine.server.service.TransactionService;
import com.paymentengine.server.service.UserService;
import com.paymentengine.server.service.WalletService;
import com.paymentengine.server.simulation.PaymentScheduler;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class PaymentEngineApplication {
    
    @Autowired
    private PaymentScheduler paymentScheduler;
    public static void main(String[] args) {
        SpringApplication.run(PaymentEngineApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Bucharest"));
        paymentScheduler.start(); 
    }
}
