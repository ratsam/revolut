package com.github.ratsam.revolut;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountService {

    Account createAccount(Integer id, BigDecimal initialBalance) throws AccountIdConstraintViolationException;

    Optional<Account> findAccount(Integer id);

    Account getAccount(Integer id) throws AccountNotFoundException;

    TransferResult transfer(Integer senderId, Integer recipientId, BigDecimal amount) throws TransferException, AccountNotFoundException;
}
