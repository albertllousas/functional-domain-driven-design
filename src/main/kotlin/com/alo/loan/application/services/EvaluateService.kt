package com.alo.loan.application.services

import arrow.core.flatMap
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.SaveLoanEvaluation
import com.alo.loan.domain.model.evaluation.AmountToLend
import com.alo.loan.domain.model.evaluation.AssessCreditRisk
import com.alo.loan.domain.model.evaluation.AssessEligibility
import com.alo.loan.domain.model.evaluation.CustomerId
import com.alo.loan.domain.model.evaluation.EvaluateLoan
import com.alo.loan.domain.model.evaluation.EvaluationId
import com.alo.loan.domain.model.evaluation.LoanApplication
import com.alo.loan.domain.model.evaluation.UnevaluatedLoan

// If you prefer you can create a class that implements the contract, it does not matter both are functions
fun evaluateService(
    assessCreditRisk: AssessCreditRisk,
    assessEligibility: AssessEligibility,
    evaluateLoan: EvaluateLoan,
    saveLoanEvaluation: SaveLoanEvaluation,
    publishEvents: PublishEvents
): Evaluate = { request ->
    request
        .loanApplication()
        .let(assessCreditRisk)
        .flatMap(assessEligibility)
        .map(evaluateLoan)
        .map { (loan, events) ->
            saveLoanEvaluation(loan)
            publishEvents(events)
        }
}

private fun LoanEvaluationRequest.loanApplication() =
    UnevaluatedLoan(EvaluationId(id), LoanApplication(CustomerId(customerId), AmountToLend(amount)))
