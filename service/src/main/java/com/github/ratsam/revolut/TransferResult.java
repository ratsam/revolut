package com.github.ratsam.revolut;

import java.io.Serializable;
import java.math.BigDecimal;

public class TransferResult implements Serializable {

    private Integer senderId;
    private BigDecimal newSenderBalance;
    private Integer recipientId;
    private BigDecimal newRecipientBalance;

    public TransferResult(Integer senderId, BigDecimal newSenderBalance, Integer recipientId, BigDecimal newRecipientBalance) {
        this.senderId = senderId;
        this.newSenderBalance = newSenderBalance;
        this.recipientId = recipientId;
        this.newRecipientBalance = newRecipientBalance;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public BigDecimal getNewSenderBalance() {
        return newSenderBalance;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public BigDecimal getNewRecipientBalance() {
        return newRecipientBalance;
    }
}
