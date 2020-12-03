package com.alo.loan.domain.model.loan

import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.CreditScore
import com.alo.loan.domain.model.Customer
import com.alo.loan.domain.model.CustomerCreditRisk
import com.alo.loan.domain.model.CustomerEligibility.Eligible
import com.alo.loan.domain.model.CustomerEligibility.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.CustomerEligibility.NotEligible
import com.alo.loan.domain.model.LoanRecord
import com.alo.loan.domain.model.creditRiskOf
import com.alo.loan.domain.model.eligibilityOf
import com.alo.loan.fixtures.buildAmountToLend
import com.alo.loan.fixtures.buildCustomer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CustomerShould {

    @Nested
    inner class CreditRiskOf {

        private val assess = Customer.Companion::creditRiskOf

        @Test
        fun `consider too risky a bad credit score`() {
            assertThat(assess(buildAmountToLend(), CreditScore.Bad)).isEqualTo(CustomerCreditRisk.TooRisky)
        }

        @Test
        fun `consider too risky a poor credit score`() {
            assertThat(assess(buildAmountToLend(), CreditScore.Poor)).isEqualTo(CustomerCreditRisk.TooRisky)
        }

        @Test
        fun `consider too risky amounts to lend over 1000 for a fair credit score`() {
            assertThat(assess(AmountToLend(1001.toBigDecimal()), CreditScore.Fair))
                .isEqualTo(CustomerCreditRisk.TooRisky)
        }

        @Test
        fun `consider as a low risk amounts to lend less than 1000 for a fair credit score`() {
            assertThat(assess(AmountToLend(999.toBigDecimal()), CreditScore.Fair))
                .isEqualTo(CustomerCreditRisk.Low)
        }

        @Test
        fun `consider too risky amounts to lend over 5000 for a good credit score`() {
            assertThat(assess(AmountToLend(5001.toBigDecimal()), CreditScore.Good))
                .isEqualTo(CustomerCreditRisk.TooRisky)
        }

        @Test
        fun `consider as a low risk amounts to lend less than  5000 for a good credit score`() {
            assertThat(assess(AmountToLend(4999.toBigDecimal()), CreditScore.Good))
                .isEqualTo(CustomerCreditRisk.Low)
        }

        @Test
        fun `consider a manual evaluation amounts to lend over 5000 for an excellent credit score`() {
            assertThat(assess(AmountToLend(10001.toBigDecimal()), CreditScore.Excellent))
                .isEqualTo(CustomerCreditRisk.ManualRiskAssessmentRequired)
        }

        @Test
        fun `consider as a low risk amounts to lend less than 10000 for an excellent credit score`() {
            assertThat(assess(AmountToLend(9999.toBigDecimal()), CreditScore.Excellent))
                .isEqualTo(CustomerCreditRisk.Low)
        }
    }

    @Nested
    class EligibilityOf {

        private val assess = Customer.Companion::eligibilityOf

        private val optimalAge = 30

        private val enoughAnnualIncomes = 25_000.toBigDecimal()

        private val optimalCustomer = buildCustomer(
            age = optimalAge,
            annualIncomes = enoughAnnualIncomes
        )

        @Test
        fun `consider eligible an optimal customer without loans`() {
            assertThat(assess(optimalCustomer, listOf())).isEqualTo(Eligible)
        }

        @Test
        fun `consider eligible an optimal customer with already payed loans`() {
            assertThat(assess(optimalCustomer, emptyList())).isEqualTo(Eligible)
        }

        @Test
        fun `consider not eligible a customer younger than 21`() {
            assertThat(assess(optimalCustomer.copy(age = 20), emptyList())).isEqualTo(NotEligible.InvalidAge)
        }

        @Test
        fun `consider manual assessment required for a young customer with good annual incomes`() {
            val youngButWellPaid = optimalCustomer.copy(age = 20, annualIncomes = 50_000.toBigDecimal())
            assertThat(assess(youngButWellPaid, emptyList())).isEqualTo(ManualEligibilityAssessmentRequired)
        }

        @Test
        fun `consider not eligible a customer younger older than 60`() {
            assertThat(assess(optimalCustomer.copy(age = 61), emptyList())).isEqualTo(NotEligible.InvalidAge)
        }

        @Test
        fun `consider not eligible a customer with not enough annual incomes`() {
            assertThat(assess(optimalCustomer.copy(annualIncomes = 1000.toBigDecimal()), emptyList()))
                .isEqualTo(NotEligible.NotEnoughAnnualIncomes)
        }

        @Test
        fun `consider not eligible a customer with not payed loans`() {
            assertThat(assess(optimalCustomer, listOf(LoanRecord.Unpaid))).isEqualTo(NotEligible.NonPayer)
        }

        @Test
        fun `consider not eligible a customer with active loans to pay for`() {
            assertThat(assess(optimalCustomer, listOf(LoanRecord.Active))).isEqualTo(NotEligible.AlreadyInDebt)
        }
    }
}
