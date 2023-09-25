package com.acozac.util;

import java.math.BigDecimal;
import java.util.UUID;
import com.acozac.model.Account;

public class AccountTestHelper
{
    public static Account createAccount(UUID accountId, BigDecimal amount)
    {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setAccountBalance(amount);
        return account;
    }
}
