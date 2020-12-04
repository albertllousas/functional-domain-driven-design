package com.alo.loan.domain.model

import com.alo.loan.domain.model.CustomerEligibility.Eligible
import com.alo.loan.domain.model.CustomerEligibility.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.CustomerEligibility.NotEligible
import com.alo.loan.fixtures.buildCustomer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomerShould {

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
