package com.paymentengine.server.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Objects;

public class Payment {
    
    private int id;
    private int walletId; 
    private int toWalletCode;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;

    public Payment(){}

    public Payment(int id, int walletId, int toWalletCode, BigDecimal amount, 
                   String currency, PaymentStatus status, LocalDateTime scheduledAt, LocalDateTime createdAt){
        this.id = id;
        this.walletId = walletId;
        this.toWalletCode = toWalletCode;
        this.amount =amount;
        this.currency = currency;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
    }

    public int getId(){ return id;}
    public void setId(int id) { this.id = id;}
    
    public int getWalletId() { return walletId; }
    public void setWalletId( int walletId){
        this.walletId = walletId;
    }

    public int getToWalletCode() { return toWalletCode;}
    public void setToWalletCode(int toWalletCode) {
        this.toWalletCode = toWalletCode;
    }
        
    public BigDecimal getAmount() { return amount;}
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledAt() {return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return id == payment.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    

}
