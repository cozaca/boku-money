package com.acozac.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;
import com.acozac.model.Account;

@ApplicationScoped
public class AccountServiceImpl implements AccountService
{
    private static final Map<UUID, Account> accounts = new ConcurrentHashMap<>();

    static
    {
        UUID user1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID user2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        UUID user3 = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");

        accounts.put(user1, createAccount(user1, new BigDecimal("10000")));
        accounts.put(user2, createAccount(user2, new BigDecimal("5000")));
        accounts.put(user3, createAccount(user3, new BigDecimal("1000")));
    }

    @Override
    public Account getById(UUID accountId)
    {
        return accounts.get(accountId);
    }

    @Override
    public List<Account> getAll()
    {
        return new ArrayList<>(accounts.values());
    }

    private static Account createAccount(UUID accountId, BigDecimal amount)
    {
        Account account = new Account();
        account.setUserId(UUID.randomUUID());
        account.setAccountId(accountId);
        account.setAccountBalance(amount);
        return account;
    }
}

