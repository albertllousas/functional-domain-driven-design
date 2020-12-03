package com.alo.loan.domain.model.loan

import com.alo.loan.domain.model.CustomerCreditRisk.Low
import com.alo.loan.domain.model.CustomerCreditRisk.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.CustomerCreditRisk.TooRisky
import com.alo.loan.domain.model.CustomerEligibility.Eligible
import com.alo.loan.domain.model.CustomerEligibility.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.CustomerEligibility.NotEligible
import com.alo.loan.domain.model.Evaluation
import com.alo.loan.domain.model.LoanApplication.Companion
import com.alo.loan.domain.model.LoanApplication.Evaluated
import com.alo.loan.domain.model.evaluate
import com.alo.loan.fixtures.buildEligibilityAssessed
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoanApplicationShould {

    private val evaluate = Companion::evaluate

    @Test
    fun `require approve a loan with low risk and eligible`() {
        val loan = buildEligibilityAssessed(customerCreditRisk = Low, customerEligibility = Eligible)
        assertThat(evaluate(loan))
            .isEqualTo(Evaluated(loan.id, loan.application, loan.customerCreditRisk, loan.customerEligibility, Evaluation.Approved))
    }

    @Test
    fun `reject a loan if is too risky and not eligible`() {
        val loan = buildEligibilityAssessed(customerCreditRisk = TooRisky, customerEligibility = NotEligible.NonPayer)
        assertThat(evaluate(loan))
            .isEqualTo(
                Evaluated(
                    id = loan.id,
                    application = loan.application,
                    customerCreditRisk = loan.customerCreditRisk,
                    customerEligibility = loan.customerEligibility,
                    evaluation = Evaluation.Rejected
                )
            )
    }

    @Test
    fun `reject a loan if is too risky but eligible`() {
        val loan = buildEligibilityAssessed(customerCreditRisk = TooRisky, customerEligibility = Eligible)
        assertThat(evaluate(loan))
            .isEqualTo(
                Evaluated(
                    id = loan.id,
                    application = loan.application,
                    customerCreditRisk = loan.customerCreditRisk,
                    customerEligibility = loan.customerEligibility,
                    evaluation = Evaluation.Rejected
                )
            )
    }

    @Test
    fun `reject a loan if risk is low but not eligible`() {
        val loan = buildEligibilityAssessed(customerCreditRisk = Low, customerEligibility = NotEligible.NonPayer)
        assertThat(evaluate(loan))
            .isEqualTo(
                Evaluated(
                    id = loan.id,
                    application = loan.application,
                    customerCreditRisk = loan.customerCreditRisk,
                    customerEligibility = loan.customerEligibility,
                    evaluation = Evaluation.Rejected
                )
            )
    }

    @Test
    fun `require a further verification when risk assessment required`() {
        val loan = buildEligibilityAssessed(customerCreditRisk = ManualRiskAssessmentRequired, customerEligibility = Eligible)
        assertThat(evaluate(loan))
            .isEqualTo(
                Evaluated(loan.id, loan.application, loan.customerCreditRisk, loan.customerEligibility, Evaluation.FurtherVerificationNeeded)
            )
    }

    @Test
    fun `require a further verification when eligibility assessment required`() {
        val loan = buildEligibilityAssessed(customerCreditRisk = Low, customerEligibility = ManualEligibilityAssessmentRequired)
        assertThat(evaluate(loan))
            .isEqualTo(
                Evaluated(loan.id, loan.application, loan.customerCreditRisk, loan.customerEligibility, Evaluation.FurtherVerificationNeeded)
            )
    }
}
