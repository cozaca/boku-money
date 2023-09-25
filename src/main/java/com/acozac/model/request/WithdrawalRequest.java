package com.acozac.model.request;

import java.math.BigDecimal;
import java.util.UUID;
import com.acozac.model.Destination;
import com.acozac.model.Sender;

public record WithdrawalRequest(UUID accountId, String address, BigDecimal amount) implements OperationRequest
{

    @Override
    public Sender sender()
    {
        return new Sender(accountId);
    }

    @Override
    public Destination destination()
    {
        return new Destination(null, address);
    }
}
