package com.acozac.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Operation
{
    private UUID operationId;
    private Sender sender;
    private Destination destination;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private OperationType operationType;
    private OperationStatus status;

    public UUID getOperationId()
    {
        return operationId;
    }

    public void setOperationId(UUID operationId)
    {
        this.operationId = operationId;
    }

    public Sender getSender()
    {
        return sender;
    }

    public void setSender(Sender sender)
    {
        this.sender = sender;
    }

    public Destination getDestination()
    {
        return destination;
    }

    public void setDestination(Destination destination)
    {
        this.destination = destination;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
    }

    public OperationType getOperationType()
    {
        return operationType;
    }

    public void setOperationType(OperationType operationType)
    {
        this.operationType = operationType;
    }

    public OperationStatus getStatus()
    {
        return status;
    }

    public void setStatus(OperationStatus status)
    {
        this.status = status;
    }
}
