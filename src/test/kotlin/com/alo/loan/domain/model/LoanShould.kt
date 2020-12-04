package com.alo.loan.domain.model

import com.alo.loan.domain.model.CreditRisk.Low
import com.alo.loan.domain.model.CreditRisk.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.CreditRisk.TooRisky
import com.alo.loan.domain.model.CustomerEligibility.Eligible
import com.alo.loan.domain.model.CustomerEligibility.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.CustomerEligibility.NotEligible
import com.alo.loan.domain.model.Loan.Companion
import com.alo.loan.domain.model.Loan.Evaluated
import com.alo.loan.fixtures.buildAmountToLend
import com.alo.loan.fixtures.buildApprovedLoan
import com.alo.loan.fixtures.buildEligibilityAssessed
import com.alo.loan.fixtures.buildFurtherVerificationNeededLoan
import com.alo.loan.fixtures.buildRejectedLoan
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LoanShould {

    @Nested
    inner class CreateEvents {

        private val createEvents = Loan.Companion::createEvents

        @Test
        fun `create an approved loan event when loan evaluation is approved`() {
            val approvedLoan = buildApprovedLoan()
            assertThat(createEvents(approvedLoan)).isEqualTo(listOf(LoanApproved(approvedLoan.id.value)))
        }

        @Test
        fun `create a rejected loan event when loan evaluation is approved`() {
            val rejectedLoan = buildRejectedLoan()
            assertThat(createEvents(rejectedLoan))
                .isEqualTo(listOf(LoanRejected(rejectedLoan.id.value)))
        }

        @Test
        fun `create held for further verification loan event when loan evaluation needs further verification`() {
            val furtherVerificationNeededLoan = buildFurtherVerificationNeededLoan()
            assertThat(createEvents(furtherVerificationNeededLoan))
                .isEqualTo(listOf(LoanHeldForFurtherVerification(furtherVerificationNeededLoan.id.value)))
        }
    }

    @Nested
    inner class Evaluate {

        private val evaluate = Companion::evaluate

        @Test
        fun `require approve a loan with low risk and eligible`() {
            val loan = buildEligibilityAssessed(creditRisk = Low, customerEligibility = Eligible)
            assertThat(evaluate(loan))
                .isEqualTo(Evaluated(loan.id, loan.application, loan.creditRisk, loan.customerEligibility, Evaluation.Approved))
        }

        @Test
        fun `reject a loan if is too risky and not eligible`() {
            val loan = buildEligibilityAssessed(creditRisk = TooRisky, customerEligibility = NotEligible.NonPayer)
            assertThat(evaluate(loan))
                .isEqualTo(
                    Evaluated(
                        id = loan.id,
                        application = loan.application,
                        creditRisk = loan.creditRisk,
                        customerEligibility = loan.customerEligibility,
                        evaluation = Evaluation.Rejected
                    )
                )
        }

        @Test
        fun `reject a loan if is too risky but eligible`() {
            val loan = buildEligibilityAssessed(creditRisk = TooRisky, customerEligibility = Eligible)
            assertThat(evaluate(loan))
                .isEqualTo(
                    Evaluated(
                        id = loan.id,
                        application = loan.application,
                        creditRisk = loan.creditRisk,
                        customerEligibility = loan.customerEligibility,
                        evaluation = Evaluation.Rejected
                    )
                )
        }

        @Test
        fun `reject a loan if risk is low but not eligible`() {
            val loan = buildEligibilityAssessed(creditRisk = Low, customerEligibility = NotEligible.NonPayer)
            assertThat(evaluate(loan))
                .isEqualTo(
                    Evaluated(
                        id = loan.id,
                        application = loan.application,
                        creditRisk = loan.creditRisk,
                        customerEligibility = loan.customerEligibility,
                        evaluation = Evaluation.Rejected
                    )
                )
        }

        @Test
        fun `require a further verification when risk assessment required`() {
            val loan = buildEligibilityAssessed(creditRisk = ManualRiskAssessmentRequired, customerEligibility = Eligible)
            assertThat(evaluate(loan))
                .isEqualTo(
                    Evaluated(loan.id, loan.application, loan.creditRisk, loan.customerEligibility, Evaluation.FurtherVerificationNeeded)
                )
        }

        @Test
        fun `require a further verification when eligibility assessment required`() {
            val loan = buildEligibilityAssessed(creditRisk = Low, customerEligibility = ManualEligibilityAssessmentRequired)
            assertThat(evaluate(loan))
                .isEqualTo(
                    Evaluated(loan.id, loan.application, loan.creditRisk, loan.customerEligibility, Evaluation.FurtherVerificationNeeded)
                )
        }
    }

    @Nested
    inner class CreditRiskOf {

        private val assess = Loan.Companion::creditRiskOf

        @Test
        fun `consider too risky a bad credit score`() {
            assertThat(assess(buildAmountToLend(), CreditScore.Bad)).isEqualTo(TooRisky)
        }

        @Test
        fun `consider too risky a poor credit score`() {
            assertThat(assess(buildAmountToLend(), CreditScore.Poor)).isEqualTo(TooRisky)
        }

        @Test
        fun `consider too risky amounts to lend over 1000 for a fair credit score`() {
            assertThat(assess(AmountToLend(1001.toBigDecimal()), CreditScore.Fair))
                .isEqualTo(TooRisky)
        }

        @Test
        fun `consider as a low risk amounts to lend less than 1000 for a fair credit score`() {
            assertThat(assess(AmountToLend(999.toBigDecimal()), CreditScore.Fair))
                .isEqualTo(Low)
        }

        @Test
        fun `consider too risky amounts to lend over 5000 for a good credit score`() {
            assertThat(assess(AmountToLend(5001.toBigDecimal()), CreditScore.Good))
                .isEqualTo(TooRisky)
        }

        @Test
        fun `consider as a low risk amounts to lend less than  5000 for a good credit score`() {
            assertThat(assess(AmountToLend(4999.toBigDecimal()), CreditScore.Good))
                .isEqualTo(Low)
        }

        @Test
        fun `consider a manual evaluation amounts to lend over 5000 for an excellent credit score`() {
            assertThat(assess(AmountToLend(10001.toBigDecimal()), CreditScore.Excellent))
                .isEqualTo(ManualRiskAssessmentRequired)
        }

        @Test
        fun `consider as a low risk amounts to lend less than 10000 for an excellent credit score`() {
            assertThat(assess(AmountToLend(9999.toBigDecimal()), CreditScore.Excellent))
                .isEqualTo(Low)
        }
    }
}
