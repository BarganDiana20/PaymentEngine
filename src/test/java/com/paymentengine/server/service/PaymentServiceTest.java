package com.paymentengine.server.service;

import com.paymentengine.server.model.Payment;
import com.paymentengine.server.model.PaymentStatus;
import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.repository.PaymentRepository;
import com.paymentengine.server.repository.WalletRepository;
import com.paymentengine.server.repository.TransactionRepository;
import com.paymentengine.server.service.impl.PaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPayment_success(){
        Wallet wallet = new Wallet();
        wallet.setId(1);
        wallet.setUserId(10);
        wallet.setBalance(new BigDecimal("400"));
        wallet.setCurrency("EUR");
        wallet.setActive(true);
        
        when(walletRepository.findById(1)).thenReturn(Optional.of(wallet));

        Payment insertedPayment = new Payment();
        insertedPayment.setId(5);
        insertedPayment.setWalletId(1);
        insertedPayment.setAmount(new BigDecimal("150"));
        insertedPayment.setStatus(PaymentStatus.PENDING);
        insertedPayment.setCreatedAt(LocalDateTime.now());
        //insertedPayment este obiectul returnat de mock
        //metoda processPayment() modifica acel obiect prin payment.setStatus(completed)
        when(paymentRepository.insert(any(Payment.class))).thenReturn(insertedPayment);

        //act
        Payment paymentResult = paymentService.processPayment(1, new BigDecimal("150"),  "Electricity bill");

        assertNotNull(paymentResult);
        assertEquals(PaymentStatus.COMPLETED, paymentResult.getStatus());

        verify(walletRepository).updateBalance(eq(1), eq(new BigDecimal("250"))); 
        verify(transactionRepository).insert(any(Transaction.class));
        verify(paymentRepository).updateStatus(5, PaymentStatus.COMPLETED);
    }

    @Test
    void processPayment_insufficientFunds(){
        Wallet wallet = new Wallet();
        wallet.setId(1);
        wallet.setUserId(10);
        wallet.setBalance(new BigDecimal("75"));

        Payment pendingPayment = new Payment();
        pendingPayment.setId(5);
        pendingPayment.setStatus(PaymentStatus.PENDING);

        when(walletRepository.findById(1)).thenReturn(Optional.of(wallet));
        when(paymentRepository.insert(any(Payment.class))).thenReturn(pendingPayment);

        assertThrows(IllegalStateException.class, () ->
            paymentService.processPayment(1, new BigDecimal("100"), "Water bill"));

        verify(walletRepository, never()).updateBalance(anyInt(), any());
        verify(transactionRepository, never()).insert(any());
        verify(paymentRepository).updateStatus(5, PaymentStatus.FAILED);
    }

    @Test
    void processPayment_walletNotFound(){
        when(walletRepository.findById(10)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> 
            paymentService.processPayment(10, new BigDecimal("150"), "Electricity bill"));
       
        verify(paymentRepository, never()).insert(any());
        verify(walletRepository, never()).updateBalance(anyInt(), any());
    }

    @Test
    void processPayment_ZeroAmount(){
        assertThrows(IllegalArgumentException.class, () -> 
            paymentService.processPayment(1, BigDecimal.ZERO, null));
    }

    @Test
    void processPayment_NegativeAmount(){
        assertThrows(IllegalArgumentException.class, () -> 
            paymentService.processPayment(1, new BigDecimal(("-10")), null));
    }

    @Test
    void transfer_succes(){
        Wallet fromWallet = new Wallet();
        fromWallet.setId(1);
        fromWallet.setUserId(10);
        fromWallet.setNameWallet("BRD Mastercard");
        fromWallet.setBalance(new BigDecimal("500"));
        fromWallet.setCurrency("EUR");
        fromWallet.setActive(true);

        Wallet toWallet = new Wallet();
        toWallet.setId(2);
        toWallet.setUserId(20);
        toWallet.setNameWallet("Raiffeisen Visa");
        toWallet.setBalance(new BigDecimal("200"));
        toWallet.setCurrency("EUR");
        toWallet.setActive(true);

        when(walletRepository.findById(1)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByWalletCode(123456)).thenReturn(Optional.of(toWallet));

        Payment payment = new Payment();
        payment.setId(10);
        payment.setWalletId(1);
        payment.setAmount(new BigDecimal("150"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.insert(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.transfer(1, 123456, new BigDecimal("150"), "Transfer to friend");

        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());

        verify(walletRepository).updateBalance(eq(1), eq(new BigDecimal("-150")));
        verify(walletRepository).updateBalance(2, new BigDecimal("150"));
        verify(transactionRepository, times(2)).insert(any(Transaction.class));
        verify(paymentRepository).updateStatus(10, PaymentStatus.COMPLETED);
    }

    @Test
    void transfer_insufficientFunds() {
        Wallet fromWallet = new Wallet();
        fromWallet.setId(1);
        fromWallet.setUserId(10);
        fromWallet.setBalance(new BigDecimal("50"));
        fromWallet.setCurrency("EUR");
        fromWallet.setActive(true);

        Wallet toWallet = new Wallet();
        toWallet.setId(2);
        toWallet.setUserId(20);
        toWallet.setBalance(new BigDecimal("200"));
        toWallet.setCurrency("EUR");
        toWallet.setActive(true);

        when(walletRepository.findById(1)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByWalletCode(123456)).thenReturn(Optional.of(toWallet));

        Payment payment = new Payment();
        payment.setId(10);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.insert(any(Payment.class))).thenReturn(payment);

        assertThrows(IllegalStateException.class,
                    () -> paymentService.transfer(1, 123456, new BigDecimal("150"), "Transfer"));

        // no wallet or transaction was updated
        verify(walletRepository, never()).updateBalance(anyInt(), any());
        verify(transactionRepository, never()).insert(any());
        verify(paymentRepository).updateStatus(10, PaymentStatus.FAILED);
    }

    @Test
    void transfer_toSameWallet_fails() {

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.transfer(1, 1, new BigDecimal("100"), "Test"));

        verify(walletRepository, never()).findById(anyInt());
        verify(paymentRepository, never()).insert(any());
    }

    @Test
    void transfer_shouldFail_whenCurrenciesMismatch() {

        Wallet fromWallet = new Wallet();
        fromWallet.setId(1);
        fromWallet.setUserId(3);
        fromWallet.setBalance(new BigDecimal("500"));
        fromWallet.setCurrency("EUR");
        fromWallet.setActive(true);

        Wallet toWallet = new Wallet();
        toWallet.setId(2);
        toWallet.setUserId(5);
        toWallet.setBalance(new BigDecimal("200"));
        toWallet.setCurrency("USD"); // mismatch
        toWallet.setActive(true);

        when(walletRepository.findById(1)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByWalletCode(123456)).thenReturn(Optional.of(toWallet));

        assertThrows(IllegalStateException.class,
                () -> paymentService.transfer(1, 123456, new BigDecimal("100"), "Test"));

        verify(paymentRepository, never()).insert(any());
        verify(walletRepository, never()).updateBalance(anyInt(), any());
        verify(transactionRepository, never()).insert(any());
    }

    @Test
    void transfer_fromSourceInactiveWallet_fails() {

        Wallet fromWallet = new Wallet();
        fromWallet.setId(1);
        fromWallet.setUserId(3);
        fromWallet.setBalance(new BigDecimal("500"));
        fromWallet.setCurrency("EUR");
        fromWallet.setActive(false); 

        Wallet toWallet = new Wallet();
        toWallet.setId(2);
        toWallet.setUserId(5);
        toWallet.setBalance(new BigDecimal("200"));
        toWallet.setCurrency("EUR");
        toWallet.setActive(true);

        when(walletRepository.findById(1)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByWalletCode(123456)).thenReturn(Optional.of(toWallet));

        assertThrows(IllegalStateException.class,
                () -> paymentService.transfer(1, 123456, new BigDecimal("100"), "Test"));

        verify(paymentRepository, never()).insert(any());
        verify(walletRepository, never()).updateBalance(anyInt(), any());
        verify(transactionRepository, never()).insert(any());
    }

    @Test
    void transfer_toDestinationInactiveWallet_fails() {

        Wallet fromWallet = new Wallet();
        fromWallet.setId(1);
        fromWallet.setUserId(3);
        fromWallet.setBalance(new BigDecimal("500"));
        fromWallet.setCurrency("EUR");
        fromWallet.setActive(true); 

        Wallet toWallet = new Wallet();
        toWallet.setId(2);
        toWallet.setUserId(5);
        toWallet.setBalance(new BigDecimal(200));
        toWallet.setCurrency("EUR");
        toWallet.setActive(false);

        when(walletRepository.findById(1)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByWalletCode(123456)).thenReturn(Optional.of(toWallet));

        assertThrows(IllegalStateException.class,
                () -> paymentService.transfer(1, 123456, new BigDecimal("100"), "Test"));

        verify(paymentRepository, never()).insert(any());
        verify(walletRepository, never()).updateBalance(anyInt(), any());
        verify(transactionRepository, never()).insert(any());
    }

    @Test
    void cancelPayment_fails_whenPaymentNotFound() {

        when(paymentRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.cancelPayment(10));

        verify(paymentRepository, never()).updateStatus(anyInt(), any());
    }

    @Test
    void cancelPayment_fails_whenStatusIsNotPending() {
        Payment payment = new Payment();
        payment.setId(10);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(10)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class,
                () -> paymentService.cancelPayment(10));

        verify(paymentRepository, never()).updateStatus(anyInt(), any());
    }

    @Test
    void cancelPayment_success() {
        Payment payment = new Payment();
        payment.setId(10);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(10)).thenReturn(Optional.of(payment));

        paymentService.cancelPayment(10);

        verify(paymentRepository).updateStatus(10, PaymentStatus.CANCELED);
    }

    

}


