package com.github.ratsam.revolut.inmemory;

import com.github.ratsam.revolut.Account;
import com.github.ratsam.revolut.AccountIdConstraintViolationException;
import com.github.ratsam.revolut.AccountNotFoundException;
import com.github.ratsam.revolut.InsufficientFundsException;
import com.github.ratsam.revolut.TransferException;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class InMemoryAccountServiceTest {

    InMemoryAccountService accountService;

    @Before
    public void setUp() {
        accountService = new InMemoryAccountService();
    }

    @Test
    public void testSuccessfulAccountCreation() throws Exception {
        assumeThat(accountService.findAccount(1), isEmpty());

        accountService.createAccount(1, new BigDecimal("100.99"));

        assertThat(accountService.findAccount(1), isPresentAnd(hasBalance("100.99")));
    }

    @Test(expected = AccountIdConstraintViolationException.class)
    public void testDuplicateAccountCreation() throws Exception {
        assumeThat(accountService.findAccount(1), isEmpty());

        accountService.createAccount(1, new BigDecimal("100.99"));
        accountService.createAccount(1, new BigDecimal("100.99"));
    }

    @Test
    public void testSuccessfulTransfer() throws Exception {
        accountService.createAccount(1, new BigDecimal("40"));
        accountService.createAccount(2, new BigDecimal("70"));

        accountService.transfer(1, 2, new BigDecimal("25"));

        assertThat(accountService.getAccount(1), hasBalance("15"));
        assertThat(accountService.getAccount(2), hasBalance("95"));
    }

    @Test(expected = InsufficientFundsException.class)
    public void testInsufficientFundsTransferCausesException() throws Exception {
        accountService.createAccount(1, new BigDecimal("40"));
        accountService.createAccount(2, new BigDecimal("70"));

        accountService.transfer(1, 2, new BigDecimal("45"));
    }

    @Test
    public void testInsufficientFundsTransferLeavesBalancesUnmodified() throws Exception {
        accountService.createAccount(1, new BigDecimal("40"));
        accountService.createAccount(2, new BigDecimal("70"));

        try {
            accountService.transfer(1, 2, new BigDecimal("45"));
        } catch (TransferException e) {
            // No-op. See "testInsufficientFundsTransferCausesException" for exception testing.
        }

        assertThat("Balance shouldn't change if transfer can't be completed",
                accountService.getAccount(1), hasBalance("40"));
        assertThat("Balance shouldn't change if transfer can't be completed",
                accountService.getAccount(2), hasBalance("70"));
    }

    @Test(expected = AccountNotFoundException.class)
    public void testTransferFromUnknownAccountCausesException() throws Exception {
        accountService.createAccount(2, new BigDecimal("70"));

        accountService.transfer(1, 2, new BigDecimal("25"));
    }

    @Test
    public void testTransferFromUnknownAccountLeavesBalanceUnmodified() throws Exception {
        accountService.createAccount(2, new BigDecimal("70"));

        try {
            accountService.transfer(1, 2, new BigDecimal("25"));
        } catch (AccountNotFoundException e) {
            // No-op. See "testTransferFromUnknownAccountCausesException" for exception case.
        }

        assertThat("Balance shouldn't change if transfer can't be completed",
                accountService.getAccount(2), hasBalance("70"));
    }

    @Test(expected = AccountNotFoundException.class)
    public void testTransferToUnknownAccountCausesException() throws Exception {
        accountService.createAccount(1, new BigDecimal("40"));

        accountService.transfer(1, 2, new BigDecimal("25"));
    }

    @Test
    public void testTransferToUnknownAccountLeavesBalanceUnmodified() throws Exception {
        accountService.createAccount(1, new BigDecimal("40"));

        try {
            accountService.transfer(1, 2, new BigDecimal("25"));
        } catch (AccountNotFoundException e) {
            // No-op. See "testTransferToUnknownAccountCausesException" for exception case.
        }

        assertThat("Balance shouldn't change if transfer can't be completed",
                accountService.getAccount(1), hasBalance("40"));
    }

    private Matcher<Account> hasBalance(String expectedBalance) {
        return new CustomTypeSafeMatcher<Account>("Account with balance = " + expectedBalance) {
            @Override
            protected boolean matchesSafely(Account item) {
                return item.getBalance().compareTo(new BigDecimal(expectedBalance)) == 0;
            }
        };
    }
}
