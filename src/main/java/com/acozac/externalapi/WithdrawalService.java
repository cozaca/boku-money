package com.acozac.externalapi;

import static com.acozac.externalapi.WithdrawalService.WithdrawalState.COMPLETED;
import static com.acozac.externalapi.WithdrawalService.WithdrawalState.FAILED;
import static com.acozac.externalapi.WithdrawalService.WithdrawalState.PROCESSING;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

public interface WithdrawalService
{
    /**
     * Request a withdrawal for given address and amount. Completes at random moment between 1 and 10 seconds
     *
     * @param id      - a caller generated withdrawal id, used for idempotency
     * @param address - an address withdraw to, can be any arbitrary string
     * @param amount  - an amount to withdraw (please replace T with type you want to use)
     * @throws IllegalArgumentException in case there's different address or amount for given id
     */
    void requestWithdrawal(WithdrawalId id, Address address, BigDecimal amount); // Please substitute T with preferred type

    /**
     * Return current state of withdrawal
     *
     * @param id - a withdrawal id
     * @return current state of withdrawal
     * @throws IllegalArgumentException in case there no withdrawal for the given id
     */
    WithdrawalState getRequestState(WithdrawalId id);

    enum WithdrawalState
    {
        PROCESSING, COMPLETED, FAILED
    }

    record WithdrawalId(UUID value)
    {
    }

    record Address(String value)
    {
    }
}

class WithdrawalServiceStub implements WithdrawalService
{
    private final ConcurrentMap<WithdrawalId, Withdrawal> requests = new ConcurrentHashMap<>();

    @Override
    public void requestWithdrawal(WithdrawalId id, Address address, BigDecimal amount)
    { // Please substitute T with prefered type
        final var existing = requests.putIfAbsent(id, new Withdrawal(finalState(), finaliseAt(), address, amount));
        if (existing != null && !Objects.equals(existing.address, address) && !Objects.equals(existing.amount, amount))
        {
            throw new IllegalStateException("Withdrawal request with id[%s] is already present".formatted(id));
        }
    }

    private WithdrawalState finalState()
    {
        return ThreadLocalRandom.current().nextBoolean() ? COMPLETED : FAILED;
    }

    private long finaliseAt()
    {
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(1000, 30000);
    }

    @Override
    public WithdrawalState getRequestState(WithdrawalId id)
    {
        final var request = requests.get(id);
        if (request == null)
        {
            throw new IllegalArgumentException("Request %s is not found".formatted(id));
        }
        return request.finalState();
    }

    record Withdrawal(WithdrawalState state, long finaliseAt, Address address, BigDecimal amount)
    {
        public WithdrawalState finalState()
        {
            return finaliseAt <= System.currentTimeMillis() ? state : PROCESSING;
        }
    }
}

