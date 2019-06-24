package com.github.ratsam.revolute;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Immutable account data.
 */
public interface Account extends Serializable {

    Integer getId();

    BigDecimal getBalance();
}
