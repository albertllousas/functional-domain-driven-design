package com.alo.loan.domain.model.evaluation

import com.alo.loan.domain.model.LoanApproved
import com.alo.loan.domain.model.LoanHeldForFurtherVerification
import com.alo.loan.domain.model.LoanRejected

// behaviour for Loan evaluation aggregate

// Creating a simple extension function
// simple implementation, for real production code you could have different implementations, as complex as business requires
fun LoanEvaluation.Behaviour.simpleCreditRiskAssessment(app: LoanApplication, score: CreditScore) =
    when (score) {
        is CreditScore.Bad, is CreditScore.Poor -> RiskReport.TooRisky
        is CreditScore.Fair -> if (app.amountToLend.amount > 1000.toBigDecimal()) RiskReport.TooRisky else RiskReport.Low
        is CreditScore.Good -> if (app.amountToLend.amount > 5000.toBigDecimal()) RiskReport.TooRisky else RiskReport.Low
        is CreditScore.Excellent -> if (app.amountToLend.amount > 10000.toBigDecimal()) RiskReport.ManualRiskAssessmentRequired else RiskReport.Low
    }

fun LoanEvaluation.Behaviour.simpleEligibilityAssessment(
    customer: Customer,
    loanRecords: List<LoanRecord>
): EligibilityReport =
    when {
        customer.age in 18..21 && customer.annualIncomes >= 30000.toBigDecimal() -> EligibilityReport.ManualEligibilityAssessmentRequired
        customer.age !in 21..60 -> EligibilityReport.NotEligible.InvalidAge
        customer.annualIncomes < 20000.toBigDecimal() -> EligibilityReport.NotEligible.NotEnoughAnnualIncomes
        loanRecords.contains(LoanRecord.Unpaid) -> EligibilityReport.NotEligible.NonPayer
        loanRecords.contains(LoanRecord.Active) -> EligibilityReport.NotEligible.AlreadyInDebt
        else -> EligibilityReport.Eligible
    }

// We can create a simple function or if we want to implement another type we can extend a field (it will be a fn anyways)
val LoanEvaluation.Behaviour.evaluate: EvaluateLoanApplication by lazy {
    { evaluation ->
        with(evaluation) {
            val risk = when (riskReport) {
                is RiskReport.TooRisky -> Rejected(id, application, riskReport, eligibilityReport, listOf(riskReport::class.simpleName!!))
                is RiskReport.Low -> Approved(id, application, riskReport, eligibilityReport)
                is RiskReport.ManualRiskAssessmentRequired -> FurtherVerificationNeeded(id, application, riskReport, eligibilityReport)
            }
            val eligibility = when (eligibilityReport) {
                is EligibilityReport.NotEligible -> Rejected(id, application, riskReport, eligibilityReport, listOf(eligibilityReport::class.simpleName!!))
                is EligibilityReport.Eligible -> Approved(id, application, riskReport, eligibilityReport)
                is EligibilityReport.ManualEligibilityAssessmentRequired -> FurtherVerificationNeeded(id, application, riskReport, eligibilityReport)
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
