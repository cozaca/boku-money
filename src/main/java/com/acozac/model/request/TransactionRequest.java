package com.acozac.model.request;

import java.math.BigDecimal;
import java.util.UUID;
import com.acozac.model.Destination;
import com.acozac.model.Sender;

public record TransactionRequest(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount) implements OperationRequest
{
    @Override
    public Sender sender()
    {
        return new Sender(senderAccountId);
    }

    @Override
    public Destination destination()
    {
        return new Destination(receiverAccountId, null);
    }
}
