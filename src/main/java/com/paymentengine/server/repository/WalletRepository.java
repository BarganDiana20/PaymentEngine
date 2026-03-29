package com.paymentengine.server.repository;

import com.paymentengine.server.model.Wallet;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface WalletRepository {
   
    Wallet insert(Wallet wallet);

    Optional<Wallet> findById(int id);

    Optional<Wallet> findByUserId(int userId);

    List<Wallet> findAllByUserId(int userId);

    Optional<Wallet> findByWalletCode(int walletCode);

    void updateBalance(int walletId, BigDecimal newBalance);
    
    void delete(int walletId);
    
}
