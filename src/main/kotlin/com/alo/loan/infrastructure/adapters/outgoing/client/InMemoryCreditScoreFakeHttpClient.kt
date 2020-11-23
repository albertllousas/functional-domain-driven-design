package com.alo.loan.infrastructure.adapters.outgoing.client

import com.alo.loan.domain.model.GetCreditScore
import com.alo.loan.domain.model.evaluation.CreditScore
import com.alo.loan.domain.model.evaluation.Customer

// fake http client, just for the sake of the demo
class InMemoryCreditScoreFakeHttpClient(
    private val staticCreditScoreAnswer: CreditScore
) : GetCreditScore {
    override fun invoke(customer: Customer): CreditScore = staticCreditScoreAnswer
}
