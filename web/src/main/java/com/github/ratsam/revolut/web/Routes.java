package com.github.ratsam.revolut.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ratsam.revolut.AccountService;

import static spark.Spark.*;

public class Routes {

    private final ObjectMapper mapper;
    private final AccountService accountService;

    public Routes(ObjectMapper mapper, AccountService accountService) {
        this.mapper = mapper;
        this.accountService = accountService;
    }

    public void register() {
        AccountHandler accountHandler = new AccountHandler(mapper, accountService);
        TransferHandler transferHandler = new TransferHandler(mapper, accountService);
        post("/account/", (req, res) -> {
            String content = accountHandler.createAccount(req.queryParams("accountId"), req.queryParams("balance"));
            res.type("application/json");
            return content;
        });
        get("/account/:accountId", (req, res) -> {
            String content = accountHandler.requestAccountInfo(req.params(":accountId"));
            res.type("application/json");
            return content;
        });
        post("/transfer", (req, res) -> {
            String content = transferHandler.handle(req.queryParams("senderId"), req.queryParams("recipientId"), req.queryParams("amount"));
            res.type("application/json");
            return content;
        });
    }
}
