package com.paymentengine.server.repository.impl;

import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.repository.WalletRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class WalletRepositoryImpl implements WalletRepository{
    
    private final Jdbi jdbi;

    public WalletRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Wallet insert(Wallet wallet) {
        int generatedId = jdbi.withHandle(
            handle -> handle.createUpdate("INSERT INTO wallets (user_id, code_wallet, name_wallet, balance, currency, active) VALUES (:userId, :codeWallet, :nameWallet, :balance, :currency, :active)")
            .bind("userId", wallet.getUserId())
            .bind("codeWallet", wallet.getWalletCode())
            .bind("nameWallet", wallet.getNameWallet())
            .bind("balance", wallet.getBalance())
            .bind("currency", wallet.getCurrency())
            .bind("active", wallet.isActive())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(int.class)
            .one()
        );

        wallet.setId(generatedId);
        return wallet;
    }

    @Override
    public Optional<Wallet> findById(int id) {
        return jdbi.withHandle(handle ->
             handle.createQuery("""
                SELECT 
                    id,
                    user_id AS userId,
                    code_wallet AS walletCode,
                    name_wallet AS nameWallet,
                    balance,
                    currency,
                    active
                FROM wallets
                WHERE id = :id""")
                  .bind("id", id)
                  .mapToBean(Wallet.class)
                  .findFirst()
        );
    }

    @Override
    public Optional<Wallet> findByUserId(int userId) {
        return jdbi.withHandle(handle ->
              handle.createQuery("""
                SELECT 
                id,
                user_id AS userId,
                code_wallet AS walletCode,
                name_wallet AS nameWallet,
                balance,
                currency,
                active
            FROM wallets 
            WHERE user_id = :userId""")
                  .bind("userId", userId)
                  .mapToBean(Wallet.class)
                  .findFirst()
        );
    }
    
    @Override
    public List<Wallet> findAllByUserId(int userId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT 
                id,
                user_id AS userId,
                code_wallet AS walletCode,
                name_wallet AS nameWallet,
                balance,
                currency,
                active
            FROM wallets 
            WHERE user_id = :userId""")
                .bind("userId", userId)
                .mapToBean(Wallet.class)
                .list()
        );
    }

    @Override
    public Optional<Wallet> findByWalletCode(int walletCode) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT 
                    id,
                    user_id AS userId,
                    code_wallet AS walletCode,
                    name_wallet AS nameWallet,
                    balance,
                    currency,
                    active
                FROM wallets 
                WHERE code_wallet = :codeWallet
            """)
            .bind("codeWallet", walletCode)
            .mapToBean(Wallet.class)
            .findFirst()
        );
    }

    @Override
    public void updateBalance(int walletId, BigDecimal amountChange) {
        jdbi.useHandle(handle ->
            handle.createUpdate("UPDATE wallets SET balance = balance + :amount WHERE id = :id AND (balance + :amount) >= 0")
            .bind("amount", amountChange)
            .bind("id", walletId)
            .execute()
        );
    }

    @Override
    public void delete(int walletId) {
        jdbi.useHandle(handle ->
            handle.createUpdate("DELETE FROM wallets WHERE id = :id")
                .bind("id", walletId)
                .execute()
        );
    }

}
