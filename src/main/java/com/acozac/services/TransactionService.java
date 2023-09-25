package com.acozac.services;

import static com.acozac.model.OperationStatus.COMPLETED;
import static com.acozac.model.OperationStatus.FAILED;
import static com.acozac.util.OperationFactory.createTransaction;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.acozac.model.Account;
import com.acozac.model.Operation;
import com.acozac.model.request.OperationRequest;

@ApplicationScoped
public class TransactionService implements OperationService
{
    @Inject
    private AccountService accountService;
    @Inject
    private OperationCache operationCacheCache;

    private final Lock lock = new ReentrantLock();

    @Override
    public Operation performOperation(OperationRequest operationRequest)
    {
        // A user can have multiple accounts
        UUID senderId = operationRequest.sender().senderId();
        UUID receiverId = operationRequest.destination().receiverId();
        Account sender = accountService.getById(senderId);
        Account receiver = accountService.getById(receiverId);

        BigDecimal amount = operationRequest.amount();

        lock.lock();
        try
        {
            if (!isOperationValid(sender, receiver, amount))
            {
                Operation transaction = createTransaction(senderId, receiverId, amount, FAILED);
                operationCacheCache.put(transaction.getOperationId(), transaction);
                return transaction;
            }

            sender.setAccountBalance(sender.getAccountBalance().subtract(amount));
            receiver.setAccountBalance(receiver.getAccountBalance().add(amount));

            Operation operation = createTransaction(sender.getAccountId(), receiverId, amount, COMPLETED);
            operationCacheCache.put(operation.getOperationId(), operation);

            return operation;
        }
        finally
        {
            lock.unlock();
        }
    }

    private boolean isOperationValid(Account sender, Account receiver, BigDecimal amount)
    {
        return sender != null && receiver != null && sender.getAccountBalance().compareTo(amount) >= 0;
    }
}
