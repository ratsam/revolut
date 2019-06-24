package com.github.ratsam.revolut;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ratsam.revolut.inmemory.InMemoryAccountService;
import com.github.ratsam.revolut.web.Routes;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Spark;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Even if this test passes it doesn't mean that application is correct.
 * If this test fails it means that there might be concurrent errors in transfer implementation.
 * <p/>
 * I personally don't like idea of "unit-testing" concurrent aspects of software.
 * However, I find such tests useful in some cases even if they can't be easily reproduced.
 * <p/>
 * Inclusion of this test in test suite is controlled via maven profile "functional-tests".
 */
public class WebServiceFunctionalTest {

    private int accountsNumber = 200;
    private int moneyPerAccount = 1500;
    private int transfersCount = 10000;
    private int concurrencyLevel = 32;

    private InMemoryAccountService accountService;

    OkHttpClient httpClient;
    int localPort;

    AtomicInteger successfulRequests;

    @Test
    public void testConcurrentTransfers() throws Exception {
        generateAccounts();

        concurrentlyTransferMoney();

        assertSuccessfulRequestsExist();
        assertTotalMoney();
        assertMinimalMoney();
    }

    @Before
    public void launchServerOnRandomPort() {
        httpClient = new OkHttpClient.Builder()
                .followRedirects(false)
                .build();

        Spark.threadPool(concurrencyLevel);
        Spark.port(0);

        accountService = new InMemoryAccountService();
        new Routes(new ObjectMapper(), accountService).register();

        Spark.awaitInitialization();

        localPort = Spark.port();

        successfulRequests = new AtomicInteger();
    }

    @After
    public void tearDown() {
        httpClient.dispatcher().executorService().shutdown();

        if (localPort != 0) {
            Spark.stop();
            Spark.awaitStop();

            localPort = 0;
        }
    }

    private void generateAccounts() throws Exception {
        // For simplicity we use 1-based sequence of ids
        for (int i = 1; i <= accountsNumber; i++) {
            accountService.createAccount(i, new BigDecimal(String.valueOf(moneyPerAccount)));
        }
    }

    private int genRandomId(Random random, int rejectId) {
        int id;
        do {
            id = 1 + random.nextInt(accountsNumber);
        } while (id == rejectId);
        return id;
    }

    private void concurrentlyTransferMoney() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrencyLevel);
        for (int i = 0; i < transfersCount; i++) {
            executorService.submit(() -> {
                if (this.transferMoneyBetweenRandomAccounts()) {
                    successfulRequests.incrementAndGet();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

    /**
     * Transfer money between two random accounts.
     * Transfer may fail if there is not enough money to send.
     * Returns {@code true} if request was successful, regardless of transfer status. Response code is only thing we care about here.
     *
     * @return true if request was successful, false otherwise
     */
    private boolean transferMoneyBetweenRandomAccounts() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int senderId = genRandomId(random, -1);
        int recipientId = genRandomId(random, senderId);

        FormBody body = new FormBody.Builder()
                .add("senderId", String.valueOf(senderId))
                .add("recipientId", String.valueOf(recipientId))
                .add("amount", String.valueOf(100 + random.nextInt(500)))
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:" + localPort + "/transfer")
                .post(body)
                .build();
        try (Response response = httpClient.newCall(request).execute()){
            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }

    private void assertSuccessfulRequestsExist() {
        assertThat("Expected at least half of the requests to succeed.", successfulRequests.get(), greaterThan(transfersCount / 2));
    }

    private void assertTotalMoney() throws AccountNotFoundException {
        BigDecimal totalMoney = new BigDecimal("0");
        for (int i = 1; i <= accountsNumber; i++) {
            totalMoney = totalMoney.add(accountService.getAccount(i).getBalance());
        }

        BigDecimal expectedTotalMoney = new BigDecimal(String.valueOf(moneyPerAccount)).multiply(new BigDecimal(String.valueOf(accountsNumber)));
        assertThat("Expected that total amount of money in the system doesn't change after transfers", totalMoney, Matchers.comparesEqualTo(expectedTotalMoney));
    }

    private void assertMinimalMoney() throws AccountNotFoundException {
        for (int i = 1; i <= accountsNumber; i++) {
            assertThat("Expected all accounts to have positive balance", accountService.getAccount(i).getBalance(), Matchers.greaterThanOrEqualTo(new BigDecimal("0")));
        }
    }
}
