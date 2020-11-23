package com.alo.loan.infrastructure.adapters.outgoing.client

import com.alo.loan.domain.model.CreditScore
import com.alo.loan.domain.model.Customer
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.FindCustomer
import com.alo.loan.domain.model.GetCreditScore

// fake http client, just for the sake of the demo
class InMemoryCreditScoreFakeHttpClient(
    private val staticCreditScoreAnswer: CreditScore
): GetCreditScore {
    override fun invoke(customer: Customer): CreditScore = staticCreditScoreAnswer
}
