package com.github.ratsam.revolute;

import java.io.Serializable;
import java.math.BigDecimal;

public class TransferResult implements Serializable {

    private Integer senderId;
    private BigDecimal newSenderBalance;
    private Integer recipientId;
    private BigDecimal newRecipientBalance;

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public BigDecimal getNewSenderBalance() {
        return newSenderBalance;
    }

    public void setNewSenderBalance(BigDecimal newSenderBalance) {
        this.newSenderBalance = newSenderBalance;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Integer recipientId) {
        this.recipientId = recipientId;
    }

    public BigDecimal getNewRecipientBalance() {
        return newRecipientBalance;
    }

    public void setNewRecipientBalance(BigDecimal newRecipientBalance) {
        this.newRecipientBalance = newRecipientBalance;
    }
}
