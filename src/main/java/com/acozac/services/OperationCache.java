package com.acozac.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;
import com.acozac.model.Operation;

@ApplicationScoped
public class OperationCache
{
    private final Map<UUID, Operation> cache = new ConcurrentHashMap<>();

    public void put(UUID operationId, Operation operation)
    {
        cache.put(operationId, operation);
    }

    public Operation get(UUID operationId)
    {
        return cache.get(operationId);
    }
}
