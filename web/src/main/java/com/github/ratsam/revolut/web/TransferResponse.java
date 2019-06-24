package com.github.ratsam.revolut.web;


import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;

public class TransferResponse implements Serializable {

    private boolean success;
    @Nullable
    private String errorDescription;
    @Nullable
    private BigDecimal newBalance;

    public static TransferResponse success(BigDecimal newBalance) {
        TransferResponse response = new TransferResponse();
        response.success = true;
        response.newBalance = newBalance;
        return response;
    }

    public static TransferResponse failure(String errorDescription) {
        TransferResponse response = new TransferResponse();
        response.success = false;
        response.errorDescription = errorDescription;
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }
}
