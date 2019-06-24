package com.github.ratsam.revolut.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ratsam.revolut.AccountNotFoundException;
import com.github.ratsam.revolut.AccountService;
import com.github.ratsam.revolut.InsufficientFundsException;
import com.github.ratsam.revolut.TransferResult;
import org.junit.Before;
import org.junit.Test;
import spark.HaltException;

import java.math.BigDecimal;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

public class TransferHandlerTest {

    AccountService accountServiceMock;
    TransferHandler handler;

    @Before
    public void setUp() {
        accountServiceMock = mock(AccountService.class);
        handler = new TransferHandler(new ObjectMapper(), accountServiceMock);
    }

    @Test
    public void testSuccessfulTransfer() throws Exception {
        doReturn(new TransferResult(1, new BigDecimal("20"), 2, new BigDecimal("70")))
                .when(accountServiceMock).transfer(anyInt(), anyInt(), any(BigDecimal.class));

        String response = handler.handle("1", "2", "40");

        assertThat(response, allOf(
                hasJsonPath("$.success", equalTo(true)),
                hasJsonPath("$.newBalance", equalTo(20))
        ));
    }

    @Test
    public void testInsufficientFundsTransfer() throws Exception {
        doThrow(new InsufficientFundsException())
                .when(accountServiceMock).transfer(anyInt(), anyInt(), any(BigDecimal.class));

        String response = handler.handle("1", "2", "40");

        assertThat(response, allOf(
                hasJsonPath("$.success", equalTo(false)),
                hasJsonPath("$.errorDescription", not(isEmptyOrNullString()))
        ));
    }

    @Test(expected = HaltException.class)
    public void testUnknownAccountTransfer() throws Exception {
        doThrow(new AccountNotFoundException(1))
                .when(accountServiceMock).transfer(anyInt(), anyInt(), any(BigDecimal.class));

        handler.handle("1", "2", "40");
    }
}
