package com.acozac.services;

import static com.acozac.model.OperationStatus.PROCESSING;
import static com.acozac.util.OperationFactory.createWithdrawal;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.acozac.externalapi.WithdrawalService;
import com.acozac.model.Account;
import com.acozac.model.Operation;
import com.acozac.model.OperationStatus;
import com.acozac.model.request.OperationRequest;

@ApplicationScoped
public class WithdrawalServiceProxy implements OperationService
{
    @Inject
    private AccountService accountService;
    @Inject
    private WithdrawalService withdrawalService;
    @Inject
    private OperationCache operationCache;

    private final Lock lock = new ReentrantLock();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    @PreDestroy
    void cleanup()
    {
        executorService.shutdown();
    }

    @Override
    public Operation performOperation(OperationRequest operationRequest)
    {
        Account account = accountService.getById(operationRequest.sender().senderId());
        if (!isOperationValid(operationRequest, account))
        {
            Operation withdrawal = createWithdrawal(operationRequest.sender().senderId(), operationRequest.destination().address(), operationRequest.amount(), OperationStatus.FAILED);
            operationCache.put(withdrawal.getOperationId(), withdrawal);
            return withdrawal;
        }

        WithdrawalService.WithdrawalId withdrawalId = new WithdrawalService.WithdrawalId(UUID.randomUUID());
        WithdrawalService.Address address = new WithdrawalService.Address(operationRequest.destination().address());

        Operation withdrawal = createWithdrawal(account.getAccountId(), address.value(), operationRequest.amount(), PROCESSING);

        withdrawalService.requestWithdrawal(withdrawalId, address, operationRequest.amount());

        // Schedule the task for periodic state checking
        scheduleStateChecking(withdrawal, account, withdrawalId, operationRequest);
        operationCache.put(withdrawal.getOperationId(), withdrawal);

        return withdrawal;
    }

    private void scheduleStateChecking(Operation withdrawal, Account account, WithdrawalService.WithdrawalId withdrawalId, OperationRequest operationRequest)
    {
        executorService.scheduleAtFixedRate(() -> {
            try
            {
                WithdrawalService.WithdrawalState requestState = withdrawalService.getRequestState(withdrawalId);
                lock.lock();
                // Check the withdrawal state when it's completed
                computeWithdrawalStatus(withdrawal, account, operationRequest, requestState);
            }
            catch (Exception e)
            {
                withdrawal.setStatus(OperationStatus.FAILED);
                scheduledFuture.cancel(true);
            }
            finally
            {
                lock.unlock();
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    private void computeWithdrawalStatus(Operation withdrawal, Account account, OperationRequest operationRequest, WithdrawalService.WithdrawalState requestState)
    {
        if (requestState == WithdrawalService.WithdrawalState.COMPLETED)
        {
            account.setAccountBalance(account.getAccountBalance().subtract(operationRequest.amount()));
            withdrawal.setStatus(OperationStatus.COMPLETED);
            scheduledFuture.cancel(true);
        }
        else if (requestState == WithdrawalService.WithdrawalState.FAILED)
        {
            // Handle the case where the operation failed
            withdrawal.setStatus(OperationStatus.FAILED);
            scheduledFuture.cancel(true);
        }
    }

    private boolean isOperationValid(OperationRequest operationRequest, Account account)
    {
        return account != null && account.getAccountBalance().compareTo(operationRequest.amount()) >= 0;
    }
}
