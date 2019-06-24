package com.github.ratsam.revolut;

public class AccountIdConstraintViolationException extends Exception {

    private final Integer id;

    public AccountIdConstraintViolationException(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
