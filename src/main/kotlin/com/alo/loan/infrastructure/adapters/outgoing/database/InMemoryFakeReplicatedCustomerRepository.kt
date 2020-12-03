package com.alo.loan.infrastructure.adapters.outgoing.database

import com.alo.loan.domain.model.Customer
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.FindCustomer

// fake database replicated data of the customer, just for the sake of the demo
class InMemoryFakeReplicatedCustomerRepository(
    private val staticFindCustomerAnswer: Customer?
) : FindCustomer {
    override fun invoke(p1: CustomerId): Customer? = staticFindCustomerAnswer
}
