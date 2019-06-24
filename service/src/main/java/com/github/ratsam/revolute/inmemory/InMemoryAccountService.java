package com.github.ratsam.revolute.inmemory;

import com.github.ratsam.revolute.Account;
import com.github.ratsam.revolute.AccountIdConstraintViolationException;
import com.github.ratsam.revolute.AccountNotFoundException;
import com.github.ratsam.revolute.AccountService;
import com.github.ratsam.revolute.TransferException;
import com.github.ratsam.revolute.TransferResult;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AccountService implementation that stored accounts data in memory
 * and uses double-synchronization for money transfer.
 */
public class InMemoryAccountService implements AccountService {

    private Map<Integer, InternalAccountData> storage = new ConcurrentHashMap<>();

    @Override
    public Account createAccount(Integer id, BigDecimal initialBalance) throws AccountIdConstraintViolationException {
        if (null == storage.putIfAbsent(id, new InternalAccountData(id, initialBalance))) {
            return new InMemoryAccount(id, initialBalance);
        } else {
            throw new AccountIdConstraintViolationException(id);
        }
    }

    @Override
    public Optional<Account> findAccount(Integer id) {
        InternalAccountData internalAccountData = storage.get(id);
        if (internalAccountData == null) {
            return Optional.empty();
        } else {
            return Optional.of(new InMemoryAccount(internalAccountData.id, internalAccountData.balance));
        }
    }

    @Override
    public Account getAccount(Integer id) throws AccountNotFoundException {
        return findAccount(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public TransferResult transfer(Integer senderId, Integer recipientId, BigDecimal amount) throws TransferException, AccountNotFoundException {
        throw new UnsupportedOperationException();
    }

    /**
     * I decided to keep InternalAccountData mutable for implementation simplicity, but
     * make sure it never gets exposed.
     */
    private static class InternalAccountData {

        private final Integer id;
        private BigDecimal balance;

        InternalAccountData(Integer id, BigDecimal balance) {
            this.id = id;
            this.balance = balance;
        }
    }
}
