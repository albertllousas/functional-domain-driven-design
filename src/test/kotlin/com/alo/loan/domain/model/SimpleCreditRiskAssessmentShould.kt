package com.alo.loan.domain.model

import com.alo.loan.domain.model.CreditScore.Bad
import com.alo.loan.domain.model.CreditScore.Excellent
import com.alo.loan.domain.model.CreditScore.Fair
import com.alo.loan.domain.model.CreditScore.Good
import com.alo.loan.domain.model.CreditScore.Poor
import com.alo.loan.fixtures.buildLoanApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleCreditRiskAssessmentShould {

    private val assess = LoanEvaluation.Behaviour::simpleCreditRiskAssessment

    @Test
    fun `consider too risky a bad credit score`() {
        assertThat(assess(buildLoanApplication(), Bad)).isEqualTo(RiskReport.TooRisky)
    }

    @Test
    fun `consider too risky a poor credit score`() {
        assertThat(assess(buildLoanApplication(), Poor)).isEqualTo(RiskReport.TooRisky)
    }

    @Test
    fun `consider too risky amounts to lend over 1000 for a fair credit score`() {
        val application = buildLoanApplication(amountToLend = AmountToLend(1001.toBigDecimal()))
        assertThat(assess(application, Fair)).isEqualTo(RiskReport.TooRisky)
    }

    @Test
    fun `consider as a low risk amounts to lend less than 1000 for a fair credit score`() {
        val application = buildLoanApplication(amountToLend = AmountToLend(999.toBigDecimal()))
        assertThat(assess(application, Fair)).isEqualTo(RiskReport.Low)
    }

    @Test
    fun `consider too risky amounts to lend over 5000 for a good credit score`() {
        val application = buildLoanApplication(amountToLend = AmountToLend(5001.toBigDecimal()))
        assertThat(assess(application, Good)).isEqualTo(RiskReport.TooRisky)
    }

    @Test
    fun `consider as a low risk amounts to lend less than  5000 for a good credit score`() {
        val application = buildLoanApplication(amountToLend = AmountToLend(4999.toBigDecimal()))
        assertThat(assess(application, Good)).isEqualTo(RiskReport.Low)
    }

    @Test
    fun `consider a manual evaluation amounts to lend over 5000 for an excellent credit score`() {
        val application = buildLoanApplication(amountToLend = AmountToLend(10001.toBigDecimal()))
        assertThat(assess(application, Excellent)).isEqualTo(RiskReport.ManualRiskAssessmentRequired)
    }

    @Test
    fun `consider as a low risk amounts to lend less than 10000 for an excellent credit score`() {
        val application = buildLoanApplication(amountToLend = AmountToLend(9999.toBigDecimal()))
        assertThat(assess(application, Excellent)).isEqualTo(RiskReport.Low)
    }
}
