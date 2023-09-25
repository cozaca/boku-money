package com.acozac.services;

import java.util.List;
import java.util.UUID;
import com.acozac.model.Account;

public interface AccountService
{
    Account getById(UUID accountId);

    List<Account> getAll();
}
