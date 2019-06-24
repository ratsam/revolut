package com.github.ratsam.revolut.inmemory;

import com.github.ratsam.revolut.Account;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class InMemoryAccount implements Account {

    private final Integer id;
    private final BigDecimal balance;

    public InMemoryAccount(Integer id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("balance", balance)
                .toString();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }
}
