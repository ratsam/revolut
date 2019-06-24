package com.github.ratsam.revolut;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ratsam.revolut.inmemory.InMemoryAccountService;
import com.github.ratsam.revolut.web.Routes;
import spark.Spark;

public class App {

    public static void main(String[] args) {
        new App().start();
    }

    private void start() {
        Spark.port(8080);

        ObjectMapper objectMapper = new ObjectMapper();
        AccountService accountService = new InMemoryAccountService();
        new Routes(objectMapper, accountService).register();
    }
}
