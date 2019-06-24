package com.github.ratsam.revolut.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ratsam.revolut.AccountIdConstraintViolationException;
import com.github.ratsam.revolut.AccountNotFoundException;
import com.github.ratsam.revolut.AccountService;
import com.github.ratsam.revolut.inmemory.InMemoryAccount;
import org.junit.Before;
import org.junit.Test;
import spark.HaltException;

import java.math.BigDecimal;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

public class AccountHandlerTest {

    AccountService accountServiceMock;
    AccountHandler handler;

    @Before
    public void setUp() {
        accountServiceMock = mock(AccountService.class);
        handler = new AccountHandler(new ObjectMapper(), accountServiceMock);
    }

    @Test
    public void testSuccessfulAccountCreation() throws Exception {
        doReturn(new InMemoryAccount(1, new BigDecimal("100.99")))
                .when(accountServiceMock).createAccount(eq(1), any(BigDecimal.class));

        String content = handler.createAccount("1", "100.99");

        assertThat(content, allOf(
                hasJsonPath("$.id", equalTo(1)),
                hasJsonPath("$.balance", equalTo(100.99d))
        ));
    }

    @Test(expected = HaltException.class)
    public void testDuplicateAccountCreation() throws Exception {
        doThrow(new AccountIdConstraintViolationException(1))
                .when(accountServiceMock).createAccount(eq(1), any(BigDecimal.class));

        handler.createAccount("1", "100.99");
    }

    @Test(expected = HaltException.class)
    public void testNegativeBalanceAccountCreation() throws Exception {
        try {
            handler.createAccount("1", "-100");
        } finally {
            verifyZeroInteractions(accountServiceMock);
        }
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        doReturn(new InMemoryAccount(1, new BigDecimal("100.99")))
                .when(accountServiceMock).getAccount(eq(1));

        String content = handler.requestAccountInfo("1");

        assertThat(content, allOf(
                hasJsonPath("$.id", equalTo(1)),
                hasJsonPath("$.balance", equalTo(100.99d))
        ));
    }

    @Test(expected = HaltException.class)
    public void testGetUnknownAccount() throws Exception {
        doThrow(new AccountNotFoundException(1)).when(accountServiceMock).getAccount(eq(1));

        handler.requestAccountInfo("1");
    }
}
