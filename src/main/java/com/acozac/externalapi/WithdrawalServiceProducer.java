package com.acozac.externalapi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class WithdrawalServiceProducer
{
    @Produces
    @ApplicationScoped // Use the appropriate scope
    public WithdrawalService produceExternalLibrary() {
        return new WithdrawalServiceStub();
    }
}
