package com.github.ratsam.revolut.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ratsam.revolut.AccountService;

import static spark.Spark.post;

public class Routes {

    private final ObjectMapper mapper;
    private final AccountService accountService;

    public Routes(ObjectMapper mapper, AccountService accountService) {
        this.mapper = mapper;
        this.accountService = accountService;
    }

    public void register() {
        TransferHandler transferHandler = new TransferHandler(mapper, accountService);
        post("/transfer", (req, res) -> {
            String content = transferHandler.handle(req.queryParams("senderId"), req.queryParams("recipientId"), req.queryParams("amount"));
            res.type("application/json");
            return content;
        });
    }
}
