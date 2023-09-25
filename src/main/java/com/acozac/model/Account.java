package com.acozac.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Account
{
    private UUID accountId;
    private UUID userId;
    private BigDecimal accountBalance;

    public UUID getAccountId()
    {
        return accountId;
    }

    public UUID getUserId()
    {
        return userId;
    }

    public void setUserId(UUID userId)
    {
        this.userId = userId;
    }

    public void setAccountId(UUID accountId)
    {
        this.accountId = accountId;
    }

    public BigDecimal getAccountBalance()
    {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance)
    {
        this.accountBalance = accountBalance;
    }

}
