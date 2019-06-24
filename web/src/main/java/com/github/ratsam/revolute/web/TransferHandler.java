package com.github.ratsam.revolute.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ratsam.revolute.AccountNotFoundException;
import com.github.ratsam.revolute.AccountService;
import com.github.ratsam.revolute.InsufficientFundsException;
import com.github.ratsam.revolute.TransferException;
import com.github.ratsam.revolute.TransferResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

import static spark.Spark.halt;

public class TransferHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TransferHandler.class);

    private final ObjectMapper mapper;
    private final AccountService accountService;

    public TransferHandler(ObjectMapper mapper, AccountService accountService) {
        this.mapper = mapper;
        this.accountService = accountService;
    }

    public String handle(String senderIdString, String recipientIdString, String amountString) throws IOException {
        Integer senderId;
        try {
            senderId = Integer.parseInt(senderIdString);
        } catch (NumberFormatException e) {
            throw halt(400, "Missing or incorrect value for senderId.");
        }

        Integer recipientId;
        try {
            recipientId = Integer.parseInt(recipientIdString);
        } catch (NumberFormatException e) {
            throw halt(400, "Missing or incorrect value for recipientId.");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountString);
        } catch (NumberFormatException e) {
            throw halt(400, "Missing or incorrect value for amount.");
        }

        try {
            TransferResult transfer = accountService.transfer(senderId, recipientId, amount);
            return mapper.writeValueAsString(TransferResponse.success(transfer.getNewSenderBalance()));
        } catch (AccountNotFoundException e) {
            throw halt(400, "Account " + e.getAccountId() + " not found.");
        } catch (InsufficientFundsException e) {
            return mapper.writeValueAsString(TransferResponse.failure("Insufficient funds"));
        } catch (TransferException e) {
            LOG.info("Error transferring money", e);

            throw halt(500, "Transfer error");
        }
    }
}
