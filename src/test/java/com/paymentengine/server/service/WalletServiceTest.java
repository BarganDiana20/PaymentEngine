package com.paymentengine.server.service;

import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.repository.WalletRepository;
import com.paymentengine.server.service.impl.WalletServiceImpl;


import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void createWallet_success(){
        when(walletRepository.insert(any(Wallet.class))).thenAnswer(
            invocation -> { Wallet w = invocation.getArgument(0);
                            w.setId(1);
                            return w;
                          });
        
        //create wallet with amount = 500 EUR
        Wallet Walletcreated = walletService.createWallet(1, "BRD MasterCard", new BigDecimal("500.00"), "EUR");

        assertNotNull(Walletcreated);
        assertEquals(1, Walletcreated.getId());
        assertEquals(1, Walletcreated.getUserId());
        assertEquals("BRD MasterCard", Walletcreated.getNameWallet());
        assertEquals(new BigDecimal("500.00"), Walletcreated.getBalance());
        assertEquals("EUR", Walletcreated.getCurrency());
        assertTrue(Walletcreated.isActive());
        
        verify(walletRepository, times(1)).insert(any(Wallet.class));
    }


    @Test
    void createWallet_negativeBalance_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(1, "BRD MasterCard", new BigDecimal("-100.00"), "EUR"));
        verify(walletRepository, never()).insert(any());
    }

    @Test
    void hasSufficientFunds_true(){
        Wallet wallet = new Wallet();
        wallet.setUserId(1);
        wallet.setNameWallet("BRD Mastercard");
        wallet.setBalance(new BigDecimal("300.00"));
        wallet.setCurrency("EUR");
        
        when(walletRepository.findById(1)).thenReturn(Optional.of(wallet));

        assertTrue(walletService.hasSufficientFunds(1, new BigDecimal("100.00")));
        verify(walletRepository).findById(1);
    
    }

    @Test
    void hasSufficientFunds_false() {
        Wallet wallet = new Wallet();
        wallet.setUserId(1);
        wallet.setNameWallet("BRD Mastercard");
        wallet.setBalance(new BigDecimal("50.00"));
        wallet.setCurrency("EUR");

        when(walletRepository.findById(1)).thenReturn(Optional.of(wallet));

        assertFalse(walletService.hasSufficientFunds(1, new BigDecimal("100.00")));
        verify(walletRepository).findById(1);
    }

    @Test
    void getWalletById_found() {
        Wallet wallet = new Wallet();
        wallet.setId(1);
        wallet.setUserId(1);
        wallet.setNameWallet("BRD Mastercard");
        wallet.setBalance(new BigDecimal("200.00"));
        wallet.setCurrency("EUR");

        when(walletRepository.findById(1)).thenReturn(Optional.of(wallet));

        Optional<Wallet> walletfound = walletService.getWalletById(1);
        assertTrue(walletfound.isPresent());
        assertEquals(wallet, walletfound.get());
        verify(walletRepository).findById(1);
    }

    @Test
    void getWalletById_notFound(){
       
        when(walletRepository.findById(10)).thenReturn(Optional.empty());
        Optional<Wallet> walletFound = walletService.getWalletById(10);
        assertTrue(walletFound.isEmpty());
        verify(walletRepository).findById(10);
    }

    @Test 
    void updateWalletBalance_shouldThrowException_whenAmountIsNegative(){

       assertThrows(IllegalArgumentException.class, () ->
            walletService.updateWalletBalance(1, new BigDecimal("-50.00"))
        );
        verify(walletRepository, never()).updateBalance(anyInt(), any());
    }

    @Test
    void getAllWalletsByUserId_shouldReturnAllWalletsForUser(){
        Wallet wallet1 = new Wallet();
        wallet1.setId(1);
        wallet1.setUserId(1);
        wallet1.setNameWallet("BRD Mastercard");
        wallet1.setBalance(new BigDecimal("300.00"));
        wallet1.setCurrency("EUR");
        wallet1.setActive(true);


        Wallet wallet2 = new Wallet();
        wallet2.setId(2);
        wallet2.setUserId(1);
        wallet2.setNameWallet("BT VISA");
        wallet2.setBalance(new BigDecimal("200.00"));
        wallet2.setCurrency("USD");
        wallet1.setActive(true);

        when(walletRepository.findAllByUserId(1)).thenReturn(List.of(wallet1, wallet2));

        List<Wallet> wallets = walletService.getAllWalletsByUserId(1);

        assertNotNull(wallets);
        assertEquals(2, wallets.size());

        assertEquals(wallet1, wallets.get(0));
        assertEquals(wallet2, wallets.get(1));
        verify(walletRepository).findAllByUserId(1);
    }

    @Test
    void deleteWallet_success(){
        Wallet wallet = new Wallet();
        wallet.setId(1);
        wallet.setUserId(1);

        when(walletRepository.findById(1)).thenReturn(Optional.of(wallet));

        walletService.deleteWallet(1, 1);
        verify(walletRepository).delete(1);
    }
}

