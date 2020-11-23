package com.alo.loan.application.services

import arrow.core.Either
import arrow.core.flatMap
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.SaveLoanEvaluation
import com.alo.loan.domain.model.evaluation.AmountToLend
import com.alo.loan.domain.model.evaluation.AssessCreditRisk
import com.alo.loan.domain.model.evaluation.AssessEligibility
import com.alo.loan.domain.model.evaluation.CreateEvents
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
    createEvents: CreateEvents,
    publishEvents: PublishEvents
): EvaluateLoan = { request ->
    request
        .loanApplication()
        .let(assessCreditRisk)
        .flatMap(assessEligibility)
        .map(evaluateLoanApplication)
        .peek(saveLoanEvaluation)
        .map(createEvents)
        .map(publishEvents)
}

private fun LoanEvaluationRequest.loanApplication() =
    UnevaluatedLoan(EvaluationId(id), LoanApplication(CustomerId(customerId), AmountToLend(amount)))

fun <L, R> Either<L, R>.peek(consume: (R) -> Unit): Either<L, R> = this.map(consume).let { this }
