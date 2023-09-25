package com.acozac.services;

import static com.acozac.util.AccountTestHelper.createAccount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.acozac.model.Account;
import com.acozac.model.Operation;
import com.acozac.model.OperationStatus;
import com.acozac.model.OperationType;
import com.acozac.model.request.OperationRequest;
import com.acozac.model.request.TransactionRequest;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest
{
    @Mock
    AccountService accountService;
    @Spy
    OperationCache operationCache;
    @InjectMocks
    TransactionService transactionService = new TransactionService();

    @Test
    void given_existingUserWithValidAccountBalance_then_shouldBeAbleToSendMoneyToAnotherUserAccount()
    {
        // given
        UUID senderUserId = UUID.randomUUID();
        UUID receiverUserId = UUID.randomUUID();
        Account senderAccount = createAccount(senderUserId, new BigDecimal("500"));
        Account receiverAccount = createAccount(receiverUserId, new BigDecimal("70"));

        when(accountService.getById(senderUserId)).thenReturn(senderAccount);
        when(accountService.getById(receiverUserId)).thenReturn(receiverAccount);

        //when: 'send 344 $'
        Operation transaction = transactionService.performOperation(new TransactionRequest(senderUserId, receiverUserId, new BigDecimal("344")));

        // then: "transaction performed successfully and the accounts are impacted accordingly"
        assertThat(transaction.getStatus()).isEqualTo(OperationStatus.COMPLETED);
        assertThat(transaction.getSender().senderId()).isEqualTo(senderAccount.getAccountId());
        assertThat(transaction.getDestination().receiverId()).isEqualTo(receiverAccount.getAccountId());
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("344"));
        assertThat(transaction.getTimestamp()).isNotNull();
        assertThat(transaction.getOperationType()).isEqualTo(OperationType.TRANSACTION);
        assertThat(senderAccount.getAccountBalance()).isEqualTo(new BigDecimal("156"));
        assertThat(receiverAccount.getAccountBalance()).isEqualTo(new BigDecimal("414"));
    }

    @Test
    void given_existingUserWithInsuficientBalance_then_shouldNotSendMoneyToAnotherUserAccount()
    {
        // given
        UUID senderUserId = UUID.randomUUID();
        UUID receiverUserId = UUID.randomUUID();
        Account senderAccount = createAccount(senderUserId, new BigDecimal("100"));
        Account receiverAccount = createAccount(receiverUserId, new BigDecimal("70"));

        when(accountService.getById(senderUserId)).thenReturn(senderAccount);
        when(accountService.getById(receiverUserId)).thenReturn(receiverAccount);

        //when: 'send 344 $'
        Operation transaction = transactionService.performOperation(new TransactionRequest(senderUserId, receiverUserId, new BigDecimal("344")));

        // then: "transaction performed successfully and the accounts are not impacted"
        assertThat(transaction.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(transaction.getSender().senderId()).isEqualTo(senderAccount.getAccountId());
        assertThat(transaction.getDestination().receiverId()).isEqualTo(receiverAccount.getAccountId());
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("344"));
        assertThat(transaction.getTimestamp()).isNotNull();
        assertThat(transaction.getOperationType()).isEqualTo(OperationType.TRANSACTION);
        assertThat(senderAccount.getAccountBalance()).isEqualTo(new BigDecimal("100"));
        assertThat(receiverAccount.getAccountBalance()).isEqualTo(new BigDecimal("70"));
    }

    @Test
    void shouldNotSendMoneyToUnExistingAccount()
    {
        // given
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        Account senderAccount = createAccount(senderId, new BigDecimal("100"));
        Account receiverAccount = createAccount(receiverId, new BigDecimal("70"));

        when(accountService.getById(senderId)).thenReturn(senderAccount);
        when(accountService.getById(receiverId)).thenReturn(null);

        //when: 'send 44 $'
        Operation transaction = transactionService.performOperation(new TransactionRequest(senderId, receiverId, new BigDecimal("44")));

        // then: "transaction performed successfully and the accounts are not impacted"
        assertThat(transaction.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(transaction.getSender().senderId()).isEqualTo(senderAccount.getAccountId());
        assertThat(transaction.getDestination().receiverId()).isEqualTo(receiverId);
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("44"));
        assertThat(transaction.getTimestamp()).isNotNull();
        assertThat(transaction.getOperationType()).isEqualTo(OperationType.TRANSACTION);
        assertThat(senderAccount.getAccountBalance()).isEqualTo(new BigDecimal("100"));
        assertThat(receiverAccount.getAccountBalance()).isEqualTo(new BigDecimal("70"));
    }

    @Test
    public void shouldBeAbleToPerformOperationFromMultipleThreads() throws InterruptedException
    {

        // Create a test scenario with multiple threads
        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // Create accounts with initial balances
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        Account senderAccount = createAccount(senderId, new BigDecimal("1000.00"));
        Account receiverAccount = createAccount(receiverId, new BigDecimal("500.00"));
        when(accountService.getById(senderId)).thenReturn(senderAccount);
        when(accountService.getById(receiverId)).thenReturn(receiverAccount);

        // Create a shared counter to track successful operations
        AtomicInteger successfulOperations = new AtomicInteger(0);

        for (int i = 1; i <= numThreads; i++)
        {
            int finalI = i;
            executorService.submit(() -> {
                OperationRequest transactionRequest = toTransactionRequest(senderAccount, receiverAccount, finalI);
                Operation operation = transactionService.performOperation(transactionRequest);
                if (operation.getStatus() == OperationStatus.COMPLETED)
                {
                    successfulOperations.incrementAndGet();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(successfulOperations.get()).isEqualTo(numThreads);

        // Assert the final account balances
        assertEquals(new BigDecimal("450.00"), senderAccount.getAccountBalance());
        assertEquals(new BigDecimal("1050.00"), receiverAccount.getAccountBalance());
    }

    private OperationRequest toTransactionRequest(Account senderAccount, Account receiverAccount, int opNr)
    {
        // Implement logic to create a sample OperationRequest for testing
        // Ensure that the request is valid for concurrent testing, e.g., set a random amount
        BigDecimal randomAmount = new BigDecimal(opNr * 10);
        return new TransactionRequest(senderAccount.getAccountId(), receiverAccount.getAccountId(), randomAmount);
    }
}
