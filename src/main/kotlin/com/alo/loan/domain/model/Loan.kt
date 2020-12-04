package com.alo.loan.domain.model

import com.alo.loan.domain.model.CreditRisk.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.CreditRisk.TooRisky
import com.alo.loan.domain.model.CreditScore.Bad
import com.alo.loan.domain.model.CreditScore.Excellent
import com.alo.loan.domain.model.CreditScore.Fair
import com.alo.loan.domain.model.CreditScore.Good
import com.alo.loan.domain.model.CreditScore.Poor
import com.alo.loan.domain.model.CustomerEligibility.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.CustomerEligibility.NotEligible
import com.alo.loan.domain.model.Evaluation.Approved
import com.alo.loan.domain.model.Evaluation.FurtherVerificationNeeded
import com.alo.loan.domain.model.Evaluation.Rejected
import com.alo.loan.domain.model.Loan.CustomerEligibilityAssessed
import com.alo.loan.domain.model.Loan.Evaluated

// Aggregate

sealed class Loan {
    companion object {}

    data class Created(
        val id: LoanApplicationId,
        val application: Application
    ) : Loan()

    data class CreditRiskAssessed(
        val id: LoanApplicationId,
        val application: Application,
        val creditRisk: CreditRisk
    ) : Loan()

    data class CustomerEligibilityAssessed(
        val id: LoanApplicationId,
        val application: Application,
        val creditRisk: CreditRisk,
        val customerEligibility: CustomerEligibility
    ) : Loan()

    data class Evaluated(
        val id: LoanApplicationId,
        val application: Application,
        val creditRisk: CreditRisk,
        val customerEligibility: CustomerEligibility,
        val evaluation: Evaluation
    ) : Loan()
}

enum class Evaluation { Rejected, FurtherVerificationNeeded, Approved }

data class Application(
    val customerId: CustomerId,
    val amountToLend: AmountToLend
)

sealed class CreditRisk {
    object Low : CreditRisk()
    object TooRisky : CreditRisk()
    object ManualRiskAssessmentRequired : CreditRisk()
}

// behaviour

fun Loan.Companion.creditRiskOf(
    amountToLend: AmountToLend,
    creditScore: CreditScore
): CreditRisk =
    when (creditScore) {
        is Bad, is Poor -> TooRisky
        is Fair -> if (amountToLend.amount > 1000.toBigDecimal()) TooRisky else CreditRisk.Low
        is Good -> if (amountToLend.amount > 5000.toBigDecimal()) TooRisky else CreditRisk.Low
        is Excellent -> if (amountToLend.amount > 10000.toBigDecimal()) ManualRiskAssessmentRequired else CreditRisk.Low
    }

val Loan.Companion.evaluateAndCreateEvents: EvaluateLoan by lazy {
    { loan: CustomerEligibilityAssessed -> Loan.evaluate(loan).let { Pair(it, Loan.createEvents(it)) } }
}

fun Loan.Companion.evaluate(loan: CustomerEligibilityAssessed): Evaluated =
    with(loan) {
        when {
            creditRisk is TooRisky || customerEligibility is NotEligible -> Rejected
            creditRisk is ManualRiskAssessmentRequired -> FurtherVerificationNeeded
            customerEligibility is ManualEligibilityAssessmentRequired -> FurtherVerificationNeeded
            else -> Approved
        }
    }.let { Evaluated(loan.id, loan.application, loan.creditRisk, loan.customerEligibility, it) }

fun Loan.Companion.createEvents(evaluated: Evaluated): List<DomainEvent> =
    when (evaluated.evaluation) {
        Rejected -> listOf(LoanApplicationRejected(evaluated.id.value))
        FurtherVerificationNeeded -> listOf(LoanApplicationHeldForFurtherVerification(evaluated.id.value))
        Approved -> listOf(LoanApplicationApproved(evaluated.id.value))
    }
