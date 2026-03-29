package com.paymentengine.server.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import com.paymentengine.server.model.Payment;
import com.paymentengine.server.model.PaymentStatus;
import com.paymentengine.server.model.TransactionType;
import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.model.TransactionStatus;
import com.paymentengine.server.repository.PaymentRepository;
import com.paymentengine.server.repository.TransactionRepository;
import com.paymentengine.server.repository.WalletRepository;
import com.paymentengine.server.service.PaymentService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService{
   
    private final PaymentRepository  paymentRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public PaymentServiceImpl(WalletRepository walletRepository,
                           PaymentRepository paymentRepository,
                           TransactionRepository transactionRepository){

        this.walletRepository = walletRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public Payment processPayment(int walletId, BigDecimal amount, String description){
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet with id " + walletId + " not found."));

        // create payment record PENDING
        Payment payment = new Payment();
        payment.setWalletId(walletId);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
      
        payment = paymentRepository.insert(payment);

        // check funds 
        if (wallet.getBalance().compareTo(amount) < 0) {
            paymentRepository.updateStatus(payment.getId(), PaymentStatus.FAILED);
            throw new IllegalStateException("Insufficient funds.");
        }

        // update wallet balance
        walletRepository.updateBalance(walletId, wallet.getBalance().subtract(amount));

        // create transaction
        Transaction transaction = new Transaction();
        transaction.setWalletId(walletId);
        transaction.setPaymentId(payment.getId());
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(amount);
        transaction.setDescription(description != null ? description : "No description provided.");
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCreatedAt(LocalDateTime.now());
        
        transactionRepository.insert(transaction);

        // mark payment COMPLETED
        paymentRepository.updateStatus(payment.getId(), PaymentStatus.COMPLETED);
        payment.setStatus(PaymentStatus.COMPLETED);

        return payment;
    }
    
    @Override
    @Transactional
    public Payment transfer(int fromWalletId, int toWalletCode, BigDecimal amount, String description) {

        if (fromWalletId == toWalletCode) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        
        // lock both wallets
        Wallet fromWallet = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found."));

        Wallet toWallet = walletRepository.findByWalletCode(toWalletCode)
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found."));

        if (!fromWallet.getCurrency().equals(toWallet.getCurrency())) {
            throw new IllegalStateException("Currency mismatch.");
        }

        if (!fromWallet.isActive()) {
            throw new IllegalStateException("Source wallet is inactive.");
        }

        if (!toWallet.isActive()) {
            throw new IllegalStateException("Destination wallet is inactive.");
        }

        String currency = fromWallet.getCurrency();

        // create payment
        Payment payment = new Payment();
        payment.setWalletId(fromWalletId);
        payment.setToWalletCode(toWalletCode);
        payment.setAmount(amount);
        payment.setCurrency(currency); 
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.insert(payment);

        //verify balance before applying changes
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            paymentRepository.updateStatus(payment.getId(), PaymentStatus.FAILED);
            throw new IllegalStateException("Insufficient funds.");
        }
        // update balances
        walletRepository.updateBalance(fromWalletId, amount.negate()); //debit
        walletRepository.updateBalance(toWallet.getId(), amount); //credit

        // create DEBIT transaction
        Transaction debit = new Transaction();
        debit.setWalletId(fromWalletId);
        debit.setPaymentId(payment.getId());
        debit.setType(TransactionType.DEBIT);
        debit.setStatus(TransactionStatus.COMPLETED);
        debit.setAmount(amount);
        debit.setCurrency(currency);
        debit.setCreatedAt(LocalDateTime.now());
        debit.setDescription(description != null ? description : "No description provided.");
        transactionRepository.insert(debit);

        // create CREDIT transaction
        Transaction credit = new Transaction();
        credit.setWalletId(toWallet.getId());
        credit.setPaymentId(payment.getId());
        credit.setType(TransactionType.CREDIT);
        credit.setStatus(TransactionStatus.COMPLETED);
        credit.setAmount(amount);
        credit.setCurrency(currency);
        credit.setCreatedAt(LocalDateTime.now());
        credit.setDescription(description != null ? description : "No description provided.");
        transactionRepository.insert(credit);

        paymentRepository.updateStatus(payment.getId(), PaymentStatus.COMPLETED);
        payment.setStatus(PaymentStatus.COMPLETED);

        return payment;
    }

    @Override
    @Transactional
    public Payment createScheduledPayment(Payment payment, String description) {

        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        Wallet fromWallet = walletRepository.findById(payment.getWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found."));

        Wallet toWallet = walletRepository.findByWalletCode(payment.getToWalletCode())
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found."));

        if (!fromWallet.getCurrency().equals(toWallet.getCurrency())) {
            throw new IllegalStateException("Currency mismatch.");
        }

        if (!fromWallet.isActive() || !toWallet.isActive()) {
            throw new IllegalStateException("Source wallet is inactive.");
        }

        if (!toWallet.isActive()) {
            throw new IllegalStateException("Destination wallet is inactive.");
        }

        // save payment
        payment.setCurrency(fromWallet.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.insert(payment);

        String finalDescription = (description != null && !description.isBlank()) ? description : "No description provided.";

        // create PENDING transactions
        // DEBIT (sender)
        Transaction debit = new Transaction();
        debit.setWalletId(fromWallet.getId());
        debit.setPaymentId(payment.getId());
        debit.setType(TransactionType.DEBIT);
        debit.setAmount(payment.getAmount());
        debit.setCurrency(fromWallet.getCurrency());
        debit.setStatus(TransactionStatus.PENDING);
        debit.setCreatedAt(LocalDateTime.now());
        debit.setDescription(finalDescription);
        transactionRepository.insert(debit);

        // CREDIT (recipient)
        Transaction credit = new Transaction();
        credit.setWalletId(toWallet.getId());
        credit.setPaymentId(payment.getId());
        credit.setType(TransactionType.CREDIT);
        credit.setAmount(payment.getAmount());
        credit.setCurrency(fromWallet.getCurrency());
        credit.setStatus(TransactionStatus.PENDING);
        credit.setCreatedAt(LocalDateTime.now());
        credit.setDescription(finalDescription);
        transactionRepository.insert(credit);

        return payment;
    }
    
    //processing scheduled payments
    @Override
    @Transactional
    public Payment processScheduledPayment(Payment payment) {

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return payment; 
        }

        LocalDateTime now = LocalDateTime.now();

        if (payment.getScheduledAt() != null && payment.getScheduledAt().isAfter(now)) {
            return payment;
        }

        Wallet fromWallet = walletRepository.findById(payment.getWalletId())
            .orElseThrow(() -> new IllegalArgumentException("Source wallet not found."));

        Wallet toWallet = walletRepository.findByWalletCode(payment.getToWalletCode())
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found."));

        if (fromWallet.getBalance().compareTo(payment.getAmount()) < 0) {
            paymentRepository.updateStatus(payment.getId(), PaymentStatus.FAILED);
            return payment;
        }

        // update balances 
        walletRepository.updateBalance(fromWallet.getId(), payment.getAmount().negate());
        walletRepository.updateBalance(toWallet.getId(), payment.getAmount());

        // update transactions -> mark COMPLETED
        transactionRepository.updateStatusByPaymentId(payment.getId(), TransactionStatus.COMPLETED);

        // mark payment completed
        paymentRepository.updateStatus(payment.getId(), PaymentStatus.COMPLETED);
        payment.setStatus(PaymentStatus.COMPLETED);

        return payment;
    }

    //A payment can ONLY be canceled if it's PENDING.
    @Override
    @Transactional
    public void cancelPayment(int paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment with id " + paymentId + " not found. "));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be canceled."
            );
        }

        paymentRepository.updateStatus(paymentId, PaymentStatus.CANCELED);
    }

    @Override
    public Payment getPaymentById(int paymentId){
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment with id " + paymentId + " not found."));
    }

    @Override
    public List<Payment> getPaymentsByWalletId(int walletId) {
        // we check if the wallet is exists
        walletRepository.findById(walletId).orElseThrow(() -> 
            new IllegalArgumentException("Wallet with id " + walletId + " not found."));

        return paymentRepository.findByWalletId(walletId);
    }
}




