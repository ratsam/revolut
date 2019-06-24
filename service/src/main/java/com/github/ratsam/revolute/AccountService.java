package com.github.ratsam.revolute;

import java.math.BigDecimal;

public interface AccountService {

    Account createAccount(Integer id, BigDecimal initialBalance);

    Account getAccount(Integer id) throws AccountNotFoundException;

    TransferResult transfer(Integer senderId, Integer recipientId, BigDecimal amount) throws TransferException, AccountNotFoundException;
}
