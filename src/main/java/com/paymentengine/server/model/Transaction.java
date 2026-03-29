package com.paymentengine.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
 
    private int id;
    private int walletId;
    private Integer paymentId; 
    private TransactionType type;  //credit or debit
    private BigDecimal amount;
    private String currency;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    private String counterpartyFirstName;
    private String counterpartyLastName;
    
    public Transaction(){}

    public Transaction(int id, int walletId, Integer paymentId, TransactionType type, BigDecimal amount,
                       String currency, String description, TransactionStatus status, LocalDateTime createdAt){
        this.id = id;
        this.walletId = walletId;
        this.paymentId = paymentId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {return id;}
    public void setId(int id) { this.id = id;}

    public int getWalletId() {return walletId;}
    public void setWalletId(int walletId) { this.walletId = walletId;}

    public Integer getPaymentId(){ return paymentId; }
    public void setPaymentId(Integer paymentId){
       this.paymentId = paymentId;
    }

    public TransactionType getType(){ 
        return type;
    }

    public void setType(TransactionType type) { 
        this.type = type; }
    
    public BigDecimal getAmount(){
        return amount;
    }

    public void setAmount(BigDecimal amount){
        this.amount = amount;
    }

    public String getCurrency() { 
        return currency; 
    }

    public void setCurrency(String currency) { 
        this.currency = currency; 
    }

    public String getDescription() { 
        return description; 
    }

    public void setDescription(String description) { 
        this.description = description; 
    }
    public TransactionStatus getStatus(){ 
        return status;
    }

    public void setStatus(TransactionStatus status) { 
        this.status = status; }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public String getCounterpartyFirstName() { return counterpartyFirstName; }
    public void setCounterpartyFirstName(String counterpartyFirstName) {
        this.counterpartyFirstName = counterpartyFirstName;
    }

    public String getCounterpartyLastName() { return counterpartyLastName; }
    public void setCounterpartyLastName(String counterpartyLastName) {
        this.counterpartyLastName = counterpartyLastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
         return id == that.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
               ", walletId=" + walletId +
                ", paymentId=" + paymentId +
                ", type=" + type +
                ", amount=" + amount +
                ", currency=" + currency +
                ", description=" + description +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
