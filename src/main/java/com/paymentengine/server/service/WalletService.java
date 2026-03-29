package com.paymentengine.server.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import com.paymentengine.server.model.Wallet;

public interface WalletService {
    
    Wallet createWallet(int userId, String nameWallet, BigDecimal initialBalance, String currency);

    Optional<Wallet> getWalletById(int walletId);

    Optional<Wallet> getWalletByUserId(int userId);

    Optional<Wallet> getWalletByCode(int walletCode);

    List<Wallet> getAllWalletsByUserId(int userId);

    void updateWalletBalance(int walletId, BigDecimal newBalance);
    void deleteWallet(int walletId, int userId);

    boolean hasSufficientFunds(int walletId, BigDecimal amount);
}
