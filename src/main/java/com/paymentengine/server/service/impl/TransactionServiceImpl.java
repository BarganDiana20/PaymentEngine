package com.paymentengine.server.service.impl;

import org.springframework.stereotype.Service;
import com.paymentengine.server.model.User;
import com.paymentengine.server.model.Role;
import com.paymentengine.server.model.Payment;
import com.paymentengine.server.model.Transaction;
import com.paymentengine.server.model.TransactionStatus;
import com.paymentengine.server.model.TransactionType;
import com.paymentengine.server.model.Wallet;
import com.paymentengine.server.repository.UserRepository;
import com.paymentengine.server.repository.TransactionRepository;
import com.paymentengine.server.repository.WalletRepository;
import com.paymentengine.server.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionServiceImpl(UserRepository userRepository, 
                                  TransactionRepository transactionRepository,
                                  WalletRepository walletRepository){
        this.userRepository = userRepository;                             
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    public List<Transaction> viewWalletTransactions(int walletId) {

        return transactionRepository.findByWalletId(walletId)
                .stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .toList();
    }

    @Override
    public List<Transaction> getTransactionsByUsername(String username) {       
       
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User '" + username + "' not found."));

        List<Wallet> wallets = walletRepository.findAllByUserId(user.getId());

        return wallets.stream()
                .flatMap(wallet ->
                    transactionRepository
                        .findAllByWalletIdWithCounterparty(wallet.getId())
                        .stream()
                )
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .toList();
    
    }

    @Override
    public List<Transaction> getTransactionsByWalletCode(int walletCode) {
      
        Wallet wallet = walletRepository.findByWalletCode(walletCode)
                .orElseThrow(() -> new IllegalArgumentException("Wallet with code '" + walletCode + "' not found."));

        return transactionRepository
            .findAllByWalletIdWithCounterparty(wallet.getId());
    }


    @Override
    public List<Transaction> getTransactionsWithCounterparty(int walletId) {
        return transactionRepository
                .findAllByWalletIdWithCounterparty(walletId);
    }

}
