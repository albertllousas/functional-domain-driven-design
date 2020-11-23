package com.alo.loan.domain.model.loan

import com.alo.loan.domain.model.evaluation.Approved
import com.alo.loan.domain.model.evaluation.EligibilityReport.Eligible
import com.alo.loan.domain.model.evaluation.EligibilityReport.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.NonPayer
import com.alo.loan.domain.model.evaluation.FurtherVerificationNeeded
import com.alo.loan.domain.model.evaluation.LoanEvaluation
import com.alo.loan.domain.model.evaluation.Rejected
import com.alo.loan.domain.model.evaluation.RiskReport.Low
import com.alo.loan.domain.model.evaluation.RiskReport.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.evaluation.RiskReport.TooRisky
import com.alo.loan.domain.model.evaluation.evaluate
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
