package com.acozac.model.request;

import java.math.BigDecimal;
import com.acozac.model.Destination;
import com.acozac.model.Sender;

public interface OperationRequest
{
    Sender sender();

    Destination destination();

    BigDecimal amount();

}
