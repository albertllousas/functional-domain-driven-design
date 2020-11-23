package com.alo.loan.domain.model.loan

import com.alo.loan.domain.model.evaluation.EligibilityReport.Eligible
import com.alo.loan.domain.model.evaluation.EligibilityReport.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.AlreadyInDebt
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.InvalidAge
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.NonPayer
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.NotEnoughAnnualIncomes
import com.alo.loan.domain.model.evaluation.LoanEvaluation
import com.alo.loan.domain.model.evaluation.LoanRecord.Active
import com.alo.loan.domain.model.evaluation.LoanRecord.Unpaid
import com.alo.loan.domain.model.evaluation.simpleEligibilityAssessment
import com.alo.loan.fixtures.buildCustomer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleEligibilityAssessmentShould {

    private val assess = LoanEvaluation.Behaviour::simpleEligibilityAssessment

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
        assertThat(assess(optimalCustomer.copy(age = 20), emptyList())).isEqualTo(InvalidAge)
    }

    @Test
    fun `consider manual assessment required for a young customer with good annual incomes`() {
        val youngButWellPaid = optimalCustomer.copy(age = 20, annualIncomes = 50_000.toBigDecimal())
        assertThat(assess(youngButWellPaid, emptyList())).isEqualTo(ManualEligibilityAssessmentRequired)
    }

    @Test
    fun `consider not eligible a customer younger older than 60`() {
        assertThat(assess(optimalCustomer.copy(age = 61), emptyList())).isEqualTo(InvalidAge)
    }

    @Test
    fun `consider not eligible a customer with not enough annual incomes`() {
        assertThat(assess(optimalCustomer.copy(annualIncomes = 1000.toBigDecimal()), emptyList())).isEqualTo(NotEnoughAnnualIncomes)
    }

    @Test
    fun `consider not eligible a customer with not payed loans`() {
        assertThat(assess(optimalCustomer, listOf(Unpaid))).isEqualTo(NonPayer)
    }

    @Test
    fun `consider not eligible a customer with active loans to pay for`() {
        assertThat(assess(optimalCustomer, listOf(Active))).isEqualTo(AlreadyInDebt)
    }
}
