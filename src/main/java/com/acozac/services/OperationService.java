package com.acozac.services;

import com.acozac.model.Operation;
import com.acozac.model.request.OperationRequest;

public interface OperationService
{
    Operation performOperation(OperationRequest operationRequest);
}
