package com.github.ratsam.revolut;

public class AccountNotFoundException extends Exception {

    private final Integer accountId;

    public AccountNotFoundException(Integer accountId) {
        this.accountId = accountId;
    }

    public AccountNotFoundException(Integer accountId, String message) {
        super(message);

        this.accountId = accountId;
    }

    public Integer getAccountId() {
        return accountId;
    }
}
