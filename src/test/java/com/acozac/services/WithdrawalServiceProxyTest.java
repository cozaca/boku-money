package com.acozac.services;

import static com.acozac.externalapi.WithdrawalService.WithdrawalState.COMPLETED;
import static com.acozac.util.AccountTestHelper.createAccount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.acozac.externalapi.WithdrawalService;
import com.acozac.model.Account;
import com.acozac.model.Operation;
import com.acozac.model.OperationStatus;
import com.acozac.model.OperationType;
import com.acozac.model.request.OperationRequest;
import com.acozac.model.request.WithdrawalRequest;

@ExtendWith(MockitoExtension.class)
public class WithdrawalServiceProxyTest
{
    @Mock
    AccountService accountService;
    @Mock
    WithdrawalService withdrawalService;
    @Spy
    OperationCache operationCache;
    @InjectMocks
    WithdrawalServiceProxy withdrawalServiceProxy = new WithdrawalServiceProxy();

    @Test
    void given_existingUserWithValidAccountBalance_then_shouldBeAbleToInitiateWithdrawal()
    {
        // given
        UUID senderUserId = UUID.randomUUID();
        String address = UUID.randomUUID().toString();
        BigDecimal withdrawalAmount = new BigDecimal("344");
        Account senderAccount = createAccount(senderUserId, new BigDecimal("500"));

        when(accountService.getById(senderUserId)).thenReturn(senderAccount);
        when(withdrawalService.getRequestState(any(WithdrawalService.WithdrawalId.class))).thenReturn(COMPLETED);

        //when: 'send 344 $'
        Operation withdrawal = withdrawalServiceProxy.performOperation(new WithdrawalRequest(senderUserId, address, withdrawalAmount));

        // then: "transaction performed successfully and the accounts are impacted accordingly"
        assertThat(withdrawal.getSender().senderId()).isEqualTo(senderAccount.getAccountId());
        assertThat(withdrawal.getDestination().address()).isEqualTo(address);
        assertThat(withdrawal.getAmount()).isEqualTo(new BigDecimal("344"));
        assertThat(withdrawal.getTimestamp()).isNotNull();
        assertThat(withdrawal.getOperationType()).isEqualTo(OperationType.WITHDRAWAL);
        assertThat(senderAccount.getAccountBalance()).isEqualTo(new BigDecimal("156"));
    }

    @Test
    void given_existingUserWithInsufficientBalance_then_shouldNotSendMoneyToAnotherUserAccount()
    {
        // given
        UUID senderUserId = UUID.randomUUID();
        String address = UUID.randomUUID().toString();
        Account senderAccount = createAccount(senderUserId, new BigDecimal("100"));

        when(accountService.getById(senderUserId)).thenReturn(senderAccount);

        //when: 'send 344 $'
        Operation withdrawal = withdrawalServiceProxy.performOperation(new WithdrawalRequest(senderUserId, address, new BigDecimal("344")));

        // then: "withdrawal performed successfully and the accounts are not impacted"
        assertThat(withdrawal.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(withdrawal.getSender().senderId()).isEqualTo(senderAccount.getAccountId());
        assertThat(withdrawal.getDestination().address()).isEqualTo(address);
        assertThat(withdrawal.getAmount()).isEqualTo(new BigDecimal("344"));
        assertThat(withdrawal.getTimestamp()).isNotNull();
        assertThat(withdrawal.getOperationType()).isEqualTo(OperationType.WITHDRAWAL);
        assertThat(senderAccount.getAccountBalance()).isEqualTo(new BigDecimal("100"));
    }

    @Test
    void shouldBeAbleToPerformOperationFromMultipleThreads() throws InterruptedException
    {

        // Create a test scenario with multiple threads
        int numThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // Create accounts with initial balances
        UUID senderId = UUID.randomUUID();
        String address = UUID.randomUUID().toString();
        Account senderAccount = createAccount(senderId, new BigDecimal("1000.00"));
        when(accountService.getById(senderId)).thenReturn(senderAccount);

        // Create a shared counter to track successful/failed operations
        AtomicInteger successfulOperations = new AtomicInteger(0);
        AtomicInteger failedOperations = new AtomicInteger(0);

        for (int i = 1; i <= numThreads; i++)
        {
            executorService.submit(() -> {
                OperationRequest withdrawalRequest = toWithdrawalRequest(senderAccount, address);
                Operation operation = withdrawalServiceProxy.performOperation(withdrawalRequest);
                if (operation.getStatus() == OperationStatus.COMPLETED)
                {
                    successfulOperations.incrementAndGet();
                    failedOperations.incrementAndGet();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        // Check that the total number of successful operations matches the expected number of threads
        assertThat(successfulOperations.get()).isEqualTo(failedOperations.addAndGet(failedOperations.get()));

        // Assert the final account balances
        assertThat(senderAccount.getAccountBalance())
            .isEqualTo(senderAccount.getAccountBalance().subtract(new BigDecimal("20").multiply(new BigDecimal(successfulOperations.get()))));
    }

    private OperationRequest toWithdrawalRequest(Account senderAccount, String address)
    {
        return new WithdrawalRequest(senderAccount.getAccountId(), address, new BigDecimal("20"));
    }
}
