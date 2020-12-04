package com.alo.loan.application.services

import arrow.core.flatMap
import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.Application
import com.alo.loan.domain.model.AssessCreditRisk
import com.alo.loan.domain.model.AssessEligibility
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.EvaluateLoan
import com.alo.loan.domain.model.Loan
import com.alo.loan.domain.model.LoanApplicationId
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.SaveLoan

// If you prefer you can create a class that implements the contract, it does not matter both are functions
fun evaluateService(
    assessCreditRisk: AssessCreditRisk,
    assessEligibility: AssessEligibility,
    evaluateLoan: EvaluateLoan,
    saveLoan: SaveLoan,
    publishEvents: PublishEvents
): Evaluate = { request ->
    request
        .loan()
        .let(assessCreditRisk)
        .flatMap(assessEligibility)
        .map(evaluateLoan)
        .map { (loan, events) ->
            saveLoan(loan)
            publishEvents(events)
        }
}

private fun LoanEvaluationRequest.loan() =
    Loan.Created(LoanApplicationId(id), Application(CustomerId(customerId), AmountToLend(amount)))
