package com.acozac.util;

import static com.acozac.model.OperationType.TRANSACTION;
import static com.acozac.model.OperationType.WITHDRAWAL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.acozac.model.Destination;
import com.acozac.model.Operation;
import com.acozac.model.OperationStatus;
import com.acozac.model.OperationType;
import com.acozac.model.Sender;

public class OperationFactory
{
    public static Operation createTransaction(
        UUID senderId,
        UUID receiverId,
        BigDecimal amount,
        OperationStatus status
    )
    {
       return createOperation(new Sender(senderId), new Destination(receiverId, null), amount, TRANSACTION, status);
    }

    public static Operation createWithdrawal(
        UUID senderId,
        String address,
        BigDecimal amount,
        OperationStatus status
    )
    {
        return createOperation(new Sender(senderId), new Destination(null, address), amount, WITHDRAWAL, status);
    }

    private static Operation createOperation(
        Sender sender,
        Destination destination,
        BigDecimal amount,
        OperationType operationType,
        OperationStatus status)
    {
        Operation operation = new Operation();
        operation.setOperationId(UUID.randomUUID());
        operation.setSender(sender);
        operation.setDestination(destination);
        operation.setAmount(amount);
        operation.setOperationType(operationType);
        operation.setStatus(status);
        operation.setTimestamp(LocalDateTime.now());
        return operation;
    }
}
