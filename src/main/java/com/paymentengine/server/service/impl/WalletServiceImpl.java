package com.paymentengine.server.service.impl;

import java.math.BigDecimal;
import java.util.Random;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.repository.WalletRepository;
import com.paymentengine.server.service.WalletService;

@Service
public class WalletServiceImpl implements WalletService{
    
    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository){
           this.walletRepository = walletRepository;
    }

    //we create the wallet with an initial balance and make sure it is positive
    @Override
    public Wallet createWallet(int userId, String walletName, BigDecimal initialBalance, String currency) {
       
        if (walletName == null || walletName.isBlank())
            throw new IllegalArgumentException("Wallet name required");

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        
        if (currency == null || currency.length() != 3)
            throw new IllegalArgumentException("Currency must be ISO 3 code");
        

        Random random = new Random();
        int walletCode;
        do {
            walletCode = 100_000 + random.nextInt(900_000);
        } while (walletRepository.findByWalletCode(walletCode).isPresent()); 


        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setWalletCode(walletCode);
        wallet.setNameWallet(walletName);
        wallet.setBalance(initialBalance);
        wallet.setCurrency(currency.toUpperCase());
        wallet.setActive(true);

        return walletRepository.insert(wallet);
    }

    @Override
    public Optional<Wallet> getWalletById(int walletId) {
        return walletRepository.findById(walletId);
    }

    @Override
    public Optional<Wallet> getWalletByUserId(int userId) {
        return walletRepository.findByUserId(userId);
    }
    
    @Override
    public List<Wallet> getAllWalletsByUserId(int userId) {
        return walletRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<Wallet> getWalletByCode(int walletCode) {
        return walletRepository.findByWalletCode(walletCode);
    }

    @Override
    public void updateWalletBalance(int walletId, BigDecimal newBalance) {
        
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }
        walletRepository.updateBalance(walletId, newBalance);
    }

    @Override
    @Transactional
    public void deleteWallet(int walletId, int userId) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // comparam userId-ul direct cu wallet.getUserId()
        if (wallet.getUserId() != userId) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to delete this wallet"
            );
        }

        walletRepository.delete(walletId);
    }

    @Override
    public boolean hasSufficientFunds(int walletId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        Optional<Wallet> walletOpt = walletRepository.findById(walletId);

        if (walletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet with id " + walletId + " not found.");
        }

        return walletOpt.get().getBalance().compareTo(amount) >= 0;
    }

}

