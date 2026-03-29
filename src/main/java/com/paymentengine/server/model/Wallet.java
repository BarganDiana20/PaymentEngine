package com.paymentengine.server.model;

import java.util.Objects;
import java.math.BigDecimal;


public class Wallet {
    
    private int id;
    private int userId;
    private int codeWallet;
    private String nameWallet;
    private BigDecimal balance;
    private String currency;
    private boolean active;

    public Wallet() {}

    public Wallet(int id, int userId, int codeWallet, String nameWallet, BigDecimal balance, String currency, boolean active) {
        this.id = id;
        this.userId = userId;
        this.codeWallet = codeWallet;
        this.nameWallet = nameWallet;
        this.balance = balance;
        this.currency = currency;
        this.active = active;
    }

    public int getId(){ return id;}
    public void setId(int id) {
        this.id = id;
    }

    public int getUserId(){ return userId;}
    public void setUserId(int userId) { 
        this.userId = userId;
    }

    public int getWalletCode() { return codeWallet; }
    public void setWalletCode(int codeWallet) { 
        this.codeWallet = codeWallet; 
    }

    public String getNameWallet() { return nameWallet; }
    public void setNameWallet(String nameWallet) { 
        this.nameWallet = nameWallet; 
    }


    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { 
        this.balance = balance; 
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { 
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wallet)) return false;
        Wallet wallet = (Wallet) o;
        return id == wallet.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
    
