package com.alo.loan.domain.model

import com.alo.loan.domain.model.CreditScore.Bad
import com.alo.loan.domain.model.CreditScore.Excellent
import com.alo.loan.domain.model.CreditScore.Fair
import com.alo.loan.domain.model.CreditScore.Good
import com.alo.loan.domain.model.CreditScore.Poor
import com.alo.loan.domain.model.EligibilityReport.Eligible
import com.alo.loan.domain.model.EligibilityReport.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.EligibilityReport.NotEligible
import com.alo.loan.domain.model.EligibilityReport.NotEligible.AlreadyInDebt
import com.alo.loan.domain.model.EligibilityReport.NotEligible.InvalidAge
import com.alo.loan.domain.model.EligibilityReport.NotEligible.NonPayer
import com.alo.loan.domain.model.EligibilityReport.NotEligible.NotEnoughAnnualIncomes
import com.alo.loan.domain.model.RiskReport.Low
import com.alo.loan.domain.model.RiskReport.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.RiskReport.TooRisky
import java.math.BigDecimal
import java.util.UUID

// Algebraic data types for LoanEvaluation Aggregate

sealed class LoanEvaluation {
    companion object Behaviour
}

data class UnevaluatedLoan(val id: EvaluationId, val application: LoanApplication) : LoanEvaluation()

data class RiskAssessed(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport
) : LoanEvaluation()

data class EvaluableLoan(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport,
    val eligibilityReport: EligibilityReport
) : LoanEvaluation()

sealed class EvaluatedLoan : LoanEvaluation()

data class Rejected(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport,
    val eligibilityReport: EligibilityReport,
    val reasons: List<String>
) : EvaluatedLoan()

data class FurtherVerificationNeeded(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport,
    val eligibilityReport: EligibilityReport
) : EvaluatedLoan()

data class Approved(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport,
    val eligibilityReport: EligibilityReport
) : EvaluatedLoan()

// Entities & Value Objects

data class LoanApplication(
    val customerId: CustomerId,
    val amountToLend: AmountToLend
)

sealed class RiskReport {
    object Low : RiskReport()
    object TooRisky : RiskReport()
    object ManualRiskAssessmentRequired : RiskReport()
}

sealed class EligibilityReport {
    object Eligible : EligibilityReport()
    object ManualEligibilityAssessmentRequired : EligibilityReport()
    sealed class NotEligible : EligibilityReport() {
        object InvalidAge : NotEligible()
        object NotEnoughAnnualIncomes : NotEligible()
        object NonPayer : NotEligible()
        object AlreadyInDebt : NotEligible()
    }
}

data class EvaluationId(val value: UUID)

data class AmountToLend(val amount: BigDecimal)

// Other entities and value object required by this aggregate,
// They could become new aggregates if they grow

sealed class LoanRecord {
    object Active : LoanRecord()
    object PaidOff : LoanRecord()
    object Unpaid : LoanRecord()
}

data class CustomerId(val value: UUID)

data class Customer(
    val id: CustomerId,
    val fullName: String,
    val address: String,
    val age: Int,
    val annualIncomes: BigDecimal
)

sealed class CreditScore {
    object Bad : CreditScore()
    object Poor : CreditScore()
    object Fair : CreditScore()
    object Good : CreditScore()
    object Excellent : CreditScore()
}

// behaviour for Loan evaluation aggregate

// Creating a simple extension function
// simple implementation, for real production code you could have different implementations, as complex as business requires
fun LoanEvaluation.Behaviour.simpleCreditRiskAssessment(app: LoanApplication, score: CreditScore) =
    when (score) {
        is Bad, is Poor -> TooRisky
        is Fair -> if (app.amountToLend.amount > 1000.toBigDecimal()) TooRisky else Low
        is Good -> if (app.amountToLend.amount > 5000.toBigDecimal()) TooRisky else Low
        is Excellent -> if (app.amountToLend.amount > 10000.toBigDecimal()) ManualRiskAssessmentRequired else Low
    }

fun LoanEvaluation.Behaviour.simpleEligibilityAssessment(
    customer: Customer,
    loanRecords: List<LoanRecord>
): EligibilityReport =
    when {
        customer.age in 18..21 && customer.annualIncomes >= 30000.toBigDecimal() -> ManualEligibilityAssessmentRequired
        customer.age !in 21..60 -> InvalidAge
        customer.annualIncomes < 20000.toBigDecimal() -> NotEnoughAnnualIncomes
        loanRecords.contains(LoanRecord.Unpaid) -> NonPayer
        loanRecords.contains(LoanRecord.Active) -> AlreadyInDebt
        else -> Eligible
    }

// We can create a simple function or if we want to implement another type we can extend a field (it will be a fn anyways)
val LoanEvaluation.Behaviour.evaluate: EvaluateLoanApplication by lazy {
    { evaluation ->
        with(evaluation) {
            val risk = when (riskReport) {
                is TooRisky -> Rejected(id, application, riskReport, eligibilityReport, listOf(riskReport::class.simpleName!!))
                is Low -> Approved(id, application, riskReport, eligibilityReport)
                is ManualRiskAssessmentRequired -> FurtherVerificationNeeded(id, application, riskReport, eligibilityReport)
            }
            val eligibility = when (eligibilityReport) {
                is NotEligible -> Rejected(id, application, riskReport, eligibilityReport, listOf(eligibilityReport::class.simpleName!!))
                is Eligible -> Approved(id, application, riskReport, eligibilityReport)
                is ManualEligibilityAssessmentRequired -> FurtherVerificationNeeded(id, application, riskReport, eligibilityReport)
            }
            when {
                risk is Rejected && eligibility is Rejected ->
                    Rejected(id, application, riskReport, eligibilityReport, risk.reasons + eligibility.reasons)
                risk is Rejected || risk is FurtherVerificationNeeded -> risk
                eligibility is Rejected || eligibility is FurtherVerificationNeeded -> eligibility
                else -> risk
            }
        }
    }
}

val LoanEvaluation.Behaviour.createEvents: CreateEvents by lazy {
    { evaluatedLoan ->
        when (evaluatedLoan) {
            is Rejected -> listOf(LoanRejected(evaluatedLoan.id.value, evaluatedLoan.reasons))
            is FurtherVerificationNeeded -> listOf(LoanHeldForFurtherVerification(evaluatedLoan.id.value))
            is Approved -> listOf(LoanApproved(evaluatedLoan.id.value))
        }
    }
}
