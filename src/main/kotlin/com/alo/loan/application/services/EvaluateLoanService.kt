package com.alo.loan.application.services

import arrow.core.flatMap
import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.AssessCreditRisk
import com.alo.loan.domain.model.AssessEligibility
import com.alo.loan.domain.model.CreateEvents
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.EvaluateLoanApplication
import com.alo.loan.domain.model.EvaluationId
import com.alo.loan.domain.model.LoanApplication
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.SaveLoanEvaluation
import com.alo.loan.domain.model.UnevaluatedLoan

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

// If you prefer you can create a class that implements the contract, it does not matter both are functions

//class EvaluateLoanService(
//    private val assessRisk: AssessCreditRisk,
//    private val assessEligibility: AssessEligibility,
//    private val evaluateLoanApplication: EvaluateLoanApplication,
//    private val saveLoanEvaluationReport: SaveLoanEvaluationReport,
//    private val createEvents: CreateEvents,
//    private val publishEvents: PublishEvents
//) : EvaluateLoan {
//    override fun invoke(request: LoanEvaluationRequest) = TODO("Not yet implemented")
