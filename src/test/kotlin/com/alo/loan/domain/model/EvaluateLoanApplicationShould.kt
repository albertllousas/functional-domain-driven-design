package com.alo.loan.domain.model

import com.alo.loan.domain.model.EligibilityReport.*
import com.alo.loan.domain.model.EligibilityReport.NotEligible.*
import com.alo.loan.domain.model.RiskReport.*
import com.alo.loan.fixtures.buildEvaluableLoan
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EvaluateLoanApplicationShould {

    private val evaluate = LoanEvaluation.Behaviour.evaluate

    @Test
    fun `require approve a loan with low risk and eligible`() {
        val loan = buildEvaluableLoan(riskReport = Low, eligibilityReport = Eligible)
        assertThat(evaluate(loan))
            .isEqualTo(Approved(loan.id, loan.application, loan.riskReport, loan.eligibilityReport))
    }

    @Test
    fun `reject a loan if is too risky and not eligible`() {
        val loan = buildEvaluableLoan(riskReport = TooRisky, eligibilityReport = NonPayer)
        assertThat(evaluate(loan))
            .isEqualTo(
                Rejected(
                    id = loan.id,
                    application = loan.application,
                    riskReport = loan.riskReport,
                    eligibilityReport = loan.eligibilityReport,
                    reasons = listOf("TooRisky", "NonPayer")
                )
            )
    }

    @Test
    fun `reject a loan if is too risky but eligible`() {
        val loan = buildEvaluableLoan(riskReport = TooRisky, eligibilityReport = Eligible)
        assertThat(evaluate(loan))
            .isEqualTo(
                Rejected(
                    id = loan.id,
                    application = loan.application,
                    riskReport = loan.riskReport,
                    eligibilityReport = loan.eligibilityReport,
                    reasons = listOf("TooRisky")
                )
            )
    }

    @Test
    fun `reject a loan if risk is low but not eligible`() {
        val loan = buildEvaluableLoan(riskReport = Low, eligibilityReport = NonPayer)
        assertThat(evaluate(loan))
            .isEqualTo(
                Rejected(
                    id = loan.id,
                    application = loan.application,
                    riskReport = loan.riskReport,
                    eligibilityReport = loan.eligibilityReport,
                    reasons = listOf("NonPayer")
                )
            )
    }

    @Test
    fun `require a further verification when risk assessment requires to`() {
        val loan = buildEvaluableLoan(riskReport = ManualRiskAssessmentRequired, eligibilityReport = Eligible)
        assertThat(evaluate(loan))
            .isEqualTo(
                FurtherVerificationNeeded(loan.id, loan.application, loan.riskReport, loan.eligibilityReport)
            )
    }

    @Test
    fun `require a further verification when eligibility assessment requires to`() {
        val loan = buildEvaluableLoan(riskReport = Low, eligibilityReport = ManualEligibilityAssessmentRequired)
        assertThat(evaluate(loan))
            .isEqualTo(
                FurtherVerificationNeeded(loan.id, loan.application, loan.riskReport, loan.eligibilityReport)
            )
    }

}
