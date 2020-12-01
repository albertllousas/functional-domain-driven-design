package com.alo.loan.application.services

import arrow.core.flatMap
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.SaveLoanEvaluation
import com.alo.loan.domain.model.evaluation.AmountToLend
import com.alo.loan.domain.model.evaluation.AssessCreditRisk
import com.alo.loan.domain.model.evaluation.AssessEligibility
import com.alo.loan.domain.model.evaluation.CustomerId
import com.alo.loan.domain.model.evaluation.EvaluateLoanApplication
import com.alo.loan.domain.model.evaluation.EvaluationId
import com.alo.loan.domain.model.evaluation.LoanApplication
import com.alo.loan.domain.model.evaluation.UnevaluatedLoan

// If you prefer you can create a class that implements the contract, it does not matter both are functions
fun evaluateLoanService(
    assessCreditRisk: AssessCreditRisk,
    assessEligibility: AssessEligibility,
    evaluateLoanApplication: EvaluateLoanApplication,
    saveLoanEvaluation: SaveLoanEvaluation,
    publishEvents: PublishEvents
): EvaluateLoan = { request ->
    request
        .loanApplication()
        .let(assessCreditRisk)
        .flatMap(assessEligibility)
        .map(evaluateLoanApplication)
        .map { (loan, events) ->
            saveLoanEvaluation(loan)
            publishEvents(events)
        }
}

private fun LoanEvaluationRequest.loanApplication() =
    UnevaluatedLoan(EvaluationId(id), LoanApplication(CustomerId(customerId), AmountToLend(amount)))
