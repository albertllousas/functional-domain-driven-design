package com.alo.loan.domain.model

import com.alo.loan.domain.model.CustomerCreditRisk.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.CustomerCreditRisk.TooRisky
import com.alo.loan.domain.model.CustomerEligibility.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.CustomerEligibility.NotEligible
import com.alo.loan.domain.model.Evaluation.Approved
import com.alo.loan.domain.model.Evaluation.FurtherVerificationNeeded
import com.alo.loan.domain.model.Evaluation.Rejected
import com.alo.loan.domain.model.LoanApplication.EligibilityAssessed
import com.alo.loan.domain.model.LoanApplication.Evaluated

// Aggregate

sealed class LoanApplication {
    companion object

    class Created(
        val id: LoanApplicationId,
        val application: Application
    ) : LoanApplication()

    data class CreditRiskAssessed(
        val id: LoanApplicationId,
        val application: Application,
        val customerCreditRisk: CustomerCreditRisk
    ) : LoanApplication()

    data class EligibilityAssessed(
        val id: LoanApplicationId,
        val application: Application,
        val customerCreditRisk: CustomerCreditRisk,
        val customerEligibility: CustomerEligibility
    ) : LoanApplication()

    data class Evaluated(
        val id: LoanApplicationId,
        val application: Application,
        val customerCreditRisk: CustomerCreditRisk,
        val customerEligibility: CustomerEligibility,
        val evaluation: Evaluation
    ) : LoanApplication()
}

enum class Evaluation { Rejected, FurtherVerificationNeeded, Approved }

data class Application(
    val customerId: CustomerId,
    val amountToLend: AmountToLend
)

// behaviour

val LoanApplication.Companion.evaluateAndCreateEvents: EvaluateLoanApplication by lazy {
    { loan: EligibilityAssessed -> LoanApplication.evaluate(loan).let { Pair(it, LoanApplication.createEvents(it)) } }
}

fun LoanApplication.Companion.evaluate(loan: EligibilityAssessed): Evaluated =
    with(loan) {
        when {
            customerCreditRisk is TooRisky || customerEligibility is NotEligible -> Rejected
            customerCreditRisk is ManualRiskAssessmentRequired -> FurtherVerificationNeeded
            customerEligibility is ManualEligibilityAssessmentRequired -> FurtherVerificationNeeded
            else -> Approved
        }
    }.let { Evaluated(loan.id, loan.application, loan.customerCreditRisk, loan.customerEligibility, it) }

fun LoanApplication.Companion.createEvents(evaluated: Evaluated): List<DomainEvent> =
    when (evaluated.evaluation) {
        Rejected -> listOf(LoanRejected(evaluated.id.value))
        FurtherVerificationNeeded -> listOf(LoanHeldForFurtherVerification(evaluated.id.value))
        Approved -> listOf(LoanApproved(evaluated.id.value))
    }
