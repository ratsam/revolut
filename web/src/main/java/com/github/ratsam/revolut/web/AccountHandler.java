package com.github.ratsam.revolut.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ratsam.revolut.Account;
import com.github.ratsam.revolut.AccountIdConstraintViolationException;
import com.github.ratsam.revolut.AccountNotFoundException;
import com.github.ratsam.revolut.AccountService;

import java.io.IOException;
import java.math.BigDecimal;

import static spark.Spark.halt;

public class AccountHandler {

    private final ObjectMapper mapper;
    private final AccountService accountService;

    public AccountHandler(ObjectMapper mapper, AccountService accountService) {
        this.mapper = mapper;
        this.accountService = accountService;
    }

    public String createAccount(String accountIdString, String initialBalanceString) throws IOException {
        Integer accountId;
        try {
            accountId = Integer.parseInt(accountIdString);
        } catch (NumberFormatException e) {
            throw halt(400, "Missing or incorrect value for accountId");
        }

        BigDecimal initialBalance;
        try {
            initialBalance = new BigDecimal(initialBalanceString);
        } catch (NumberFormatException | NullPointerException e) {
            throw halt(400, "Missing or incorrect value for initialBalance");
        }

        if (initialBalance.compareTo(new BigDecimal("0")) < 0) {
            throw halt(400, "Missing or incorrect value for initialBalance");
        }

        try {
            Account account = accountService.createAccount(accountId, initialBalance);
            return mapper.writeValueAsString(account);
        } catch (AccountIdConstraintViolationException e) {
            throw halt(400, "Account with given id can't be created");
        }
    }

    public String requestAccountInfo(String accountIdString) throws IOException {
        Integer accountId;
        try {
            accountId = Integer.parseInt(accountIdString);
        } catch (NumberFormatException e) {
            throw halt(404);
        }

        try {
            return mapper.writeValueAsString(accountService.getAccount(accountId));
        } catch (AccountNotFoundException e) {
            throw halt(404);
        }
    }
}
