package com.alo.loan.domain.model.evaluation

import com.alo.loan.domain.model.DomainEvent
import com.alo.loan.domain.model.LoanApproved
import com.alo.loan.domain.model.LoanHeldForFurtherVerification
import com.alo.loan.domain.model.LoanRejected
import com.alo.loan.domain.model.evaluation.CreditScore.Bad
import com.alo.loan.domain.model.evaluation.CreditScore.Excellent
import com.alo.loan.domain.model.evaluation.CreditScore.Fair
import com.alo.loan.domain.model.evaluation.CreditScore.Good
import com.alo.loan.domain.model.evaluation.CreditScore.Poor
import com.alo.loan.domain.model.evaluation.EligibilityReport.Eligible
import com.alo.loan.domain.model.evaluation.EligibilityReport.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible
import com.alo.loan.domain.model.evaluation.RiskReport.Low
import com.alo.loan.domain.model.evaluation.RiskReport.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.evaluation.RiskReport.TooRisky

// behaviour for Loan evaluation aggregate

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
        customer.age !in 21..60 -> NotEligible.InvalidAge
        customer.annualIncomes < 20000.toBigDecimal() -> NotEligible.NotEnoughAnnualIncomes
        loanRecords.contains(LoanRecord.Unpaid) -> NotEligible.NonPayer
        loanRecords.contains(LoanRecord.Active) -> NotEligible.AlreadyInDebt
        else -> Eligible
    }

val LoanEvaluation.Behaviour.evaluateAndCreateEvents: EvaluateLoanApplication by lazy {
    { loan -> LoanEvaluation.evaluate(loan).let { Pair(it, LoanEvaluation.createEvents(it)) } }
}

fun LoanEvaluation.Behaviour.evaluate(loan: EligibilityAssessed): EvaluatedLoan =
    with(loan) {
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

fun LoanEvaluation.Behaviour.createEvents(evaluatedLoan: EvaluatedLoan): List<DomainEvent> =
    when (evaluatedLoan) {
        is Rejected -> listOf(LoanRejected(evaluatedLoan.id.value, evaluatedLoan.reasons))
        is FurtherVerificationNeeded -> listOf(LoanHeldForFurtherVerification(evaluatedLoan.id.value))
        is Approved -> listOf(LoanApproved(evaluatedLoan.id.value))
    }
