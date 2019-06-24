package com.github.ratsam.revolute.inmemory;

import com.github.ratsam.revolute.Account;
import com.github.ratsam.revolute.AccountIdConstraintViolationException;
import com.github.ratsam.revolute.AccountNotFoundException;
import com.github.ratsam.revolute.AccountService;
import com.github.ratsam.revolute.InsufficientFundsException;
import com.github.ratsam.revolute.TransferException;
import com.github.ratsam.revolute.TransferResult;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AccountService implementation that stores accounts data in memory
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
        InternalAccountData sender = storage.get(senderId);
        InternalAccountData recipient = storage.get(recipientId);

        if (sender == null) {
            throw new AccountNotFoundException(senderId);
        }
        if (recipient == null) {
            throw new AccountNotFoundException(recipientId);
        }

        return transfer(sender, recipient, amount);
    }

    private TransferResult transfer(InternalAccountData sender, InternalAccountData recipient, BigDecimal amount) throws InsufficientFundsException {
        return withLockedAccounts(sender, recipient, () -> {
            if (sender.balance.compareTo(amount) < 0) {
                throw new InsufficientFundsException();
            }
            sender.balance = sender.balance.subtract(amount);
            recipient.balance = recipient.balance.add(amount);
            return new TransferResult(sender.id, sender.balance, sender.id, sender.balance);
        });
    }

    private <T, E extends Exception> T withLockedAccounts(InternalAccountData accountA, InternalAccountData accountB, AccountsAction<T, E> action) throws E {
        // Avoid dead-locks by forcing lock order.
        if (accountA.id > accountB.id) {
            return withLockedAccounts(accountB, accountA, action);
        } else {
            synchronized (accountA) {
                synchronized (accountB) {
                    return action.perform();
                }
            }
        }
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

    interface AccountsAction<T, E extends Exception> {

        T perform() throws E;
    }
}
