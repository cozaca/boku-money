package com.acozac.util;

import java.math.BigDecimal;
import com.acozac.externalapi.WithdrawalService;
import com.acozac.model.Destination;
import com.acozac.model.OperationStatus;
import com.acozac.model.Sender;
import com.acozac.model.request.OperationRequest;
import com.acozac.model.request.TransactionRequest;
import com.acozac.model.request.WithdrawalRequest;

public class ToOperationConvertor
{
    public static OperationStatus from(WithdrawalService.WithdrawalState withdrawalState)
    {
        return switch (withdrawalState)
            {
                case FAILED -> OperationStatus.FAILED;
                case COMPLETED -> OperationStatus.COMPLETED;
                case PROCESSING -> OperationStatus.PROCESSING;
            };
    }

    public static OperationRequest from(TransactionRequest transactionRequest)
    {
        return new OperationRequest()
        {
            @Override
            public Sender sender()
            {
                return transactionRequest.sender();
            }

            @Override
            public Destination destination()
            {
                return transactionRequest.destination();
            }

            @Override
            public BigDecimal amount()
            {
                return transactionRequest.amount();
            }
        };
    }

    public static OperationRequest from(WithdrawalRequest withdrawalRequest)
    {
        return new OperationRequest()
        {
            @Override
            public Sender sender()
            {
                return withdrawalRequest.sender();
            }

            @Override
            public Destination destination()
            {
                return withdrawalRequest.destination();
            }

            @Override
            public BigDecimal amount()
            {
                return withdrawalRequest.amount();
            }
        };
    }
}
