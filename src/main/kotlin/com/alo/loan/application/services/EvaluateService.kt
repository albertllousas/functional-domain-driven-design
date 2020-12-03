package com.alo.loan.application.services

import arrow.core.flatMap
import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.Application
import com.alo.loan.domain.model.AssessCreditRisk
import com.alo.loan.domain.model.AssessEligibility
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.EvaluateLoanApplication
import com.alo.loan.domain.model.LoanApplication
import com.alo.loan.domain.model.LoanApplicationId
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.SaveLoanApplication

// If you prefer you can create a class that implements the contract, it does not matter both are functions
fun evaluateService(
    assessCreditRisk: AssessCreditRisk,
    assessEligibility: AssessEligibility,
    evaluateLoanApplication: EvaluateLoanApplication,
    saveLoanApplication: SaveLoanApplication,
    publishEvents: PublishEvents
): Evaluate = { request ->
    request
        .loanApplication()
        .let(assessCreditRisk)
        .flatMap(assessEligibility)
        .map(evaluateLoanApplication)
        .map { (loan, events) ->
            saveLoanApplication(loan)
            publishEvents(events)
        }
}

private fun LoanApplicationEvaluationRequest.loanApplication() =
    LoanApplication.Created(LoanApplicationId(id), Application(CustomerId(customerId), AmountToLend(amount)))
