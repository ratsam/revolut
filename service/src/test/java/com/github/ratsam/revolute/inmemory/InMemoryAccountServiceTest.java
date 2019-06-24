package com.github.ratsam.revolute.inmemory;

import com.github.ratsam.revolute.Account;
import com.github.ratsam.revolute.AccountIdConstraintViolationException;
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
    public void testAccountCreation() throws Exception {
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

    private Matcher<Account> hasBalance(String expectedBalance) {
        return new CustomTypeSafeMatcher<Account>("Account with balance = " + expectedBalance) {
            @Override
            protected boolean matchesSafely(Account item) {
                return item.getBalance().compareTo(new BigDecimal(expectedBalance)) == 0;
            }
        };
    }
}
