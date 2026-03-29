package com.paymentengine.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paymentengine.server.model.Role;
import com.paymentengine.server.model.User;
import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.model.TransactionStatus;
import com.paymentengine.server.model.TransactionType;
import com.paymentengine.server.repository.TransactionRepository;
import com.paymentengine.server.repository.UserRepository;
import com.paymentengine.server.repository.WalletRepository;
import com.paymentengine.server.service.impl.TransactionServiceImpl;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.lang.StackWalker.Option;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void viewWalletTransactions_success() {
        Transaction t1 = new Transaction();
        t1.setId(1);
        t1.setCreatedAt(LocalDateTime.now());

        Transaction t2 = new Transaction();
        t2.setId(2);
        t2.setCreatedAt(LocalDateTime.now());

        when(transactionRepository.findByWalletId(1)).thenReturn(List.of(t1, t2));

        List<Transaction> result = transactionService.viewWalletTransactions(1);

        assertEquals(2, result.size());
        assertEquals(t1, result.get(0));
        assertEquals(t2, result.get(1));

        verify(transactionRepository).findByWalletId(1);
    }

    @Test
    void viewWalletTransactions_empty() {
        when(transactionRepository.findByWalletId(1)).thenReturn(List.of());

        List<Transaction> result = transactionService.viewWalletTransactions(1);

        assertTrue(result.isEmpty());
        verify(transactionRepository).findByWalletId(1);
    }

    @Test
    void getTransactionsByUsername_success() {
        User user = new User();
        user.setId(10);
        user.setRole(Role.USER);
        user.setUsername("alex_capraru");

        Wallet wallet = new Wallet();
        wallet.setId(1);
        wallet.setUserId(10);
        wallet.setBalance(new BigDecimal("500"));
        wallet.setCurrency("EUR");
        wallet.setActive(true);

        Transaction t1 = new Transaction();
        t1.setId(1);
        t1.setWalletId(1);
        t1.setAmount(new BigDecimal("125"));
        t1.setCurrency("EUR");
        t1.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUsername("alex_capraru")).thenReturn(Optional.of(user));
        when(walletRepository.findAllByUserId(10)).thenReturn(List.of(wallet));
        when(transactionRepository.findAllByWalletIdWithCounterparty(1)).thenReturn(List.of(t1));

        List<Transaction> result = transactionService.getTransactionsByUsername("alex_capraru");

        assertEquals(1, result.size());
   
        verify(userRepository).findByUsername("alex_capraru");
        verify(walletRepository).findAllByUserId(10);
    }

    @Test
    void getTransactionsByWalletCode_success() {

        Wallet wallet = new Wallet();
        wallet.setId(1);
        wallet.setWalletCode(123456);
        wallet.setBalance(new BigDecimal("900"));
        wallet.setCurrency("LEI");
        wallet.setActive(true);

        Transaction t1 = new Transaction();
        t1.setId(1);
        t1.setWalletId(1);
        t1.setAmount(new BigDecimal("155"));
        t1.setCurrency("LEI");
        t1.setCreatedAt(LocalDateTime.of(2026,Month.FEBRUARY,27, 9,30,40,50000));

        when(walletRepository.findByWalletCode(123456)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findAllByWalletIdWithCounterparty(1)).thenReturn(List.of(t1));

        List<Transaction> result = transactionService.getTransactionsByWalletCode(123456);

        assertEquals(1, result.size());

        verify(walletRepository).findByWalletCode(123456);
    }

    @Test
    void getTransactionsWithCounterparty_success() {

        Transaction t1 = new Transaction();
        t1.setId(1);
        t1.setWalletId(1);
        t1.setAmount(new BigDecimal("150"));
        t1.setCurrency("USD");
        t1.setCreatedAt(LocalDateTime.now());

        when(transactionRepository.findAllByWalletIdWithCounterparty(1)).thenReturn(List.of(t1));

        List<Transaction> result = transactionService.getTransactionsWithCounterparty(1);

        assertEquals(1, result.size());

        verify(transactionRepository).findAllByWalletIdWithCounterparty(1);
    }

}
