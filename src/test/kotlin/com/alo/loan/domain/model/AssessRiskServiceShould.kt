package com.alo.loan.domain.model

import arrow.core.left
import arrow.core.right
import com.alo.loan.fixtures.buildLoanCreated
import com.alo.loan.fixtures.buildCreditRiskAssessed
import com.alo.loan.fixtures.buildCustomer
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AssessRiskServiceShould {

    private val findCustomer = mockk<FindCustomer>()

    private val getCreditScore = mockk<GetCreditScore>(relaxed = true)

    private val riskOf = mockk<(AmountToLend, CreditScore) -> CreditRisk>(relaxed = true)

    private val assessRisk = AssessRiskService(findCustomer, getCreditScore, riskOf)

    @Test
    fun `coordinate the risk assessment of a loan with external world dependencies`() {
        val created = buildLoanCreated()
        val customer = buildCustomer()
        val creditScore = CreditScore.Poor
        every { findCustomer(created.application.customerId) } returns customer
        every { getCreditScore(customer) } returns creditScore
        every { riskOf(created.application.amountToLend, creditScore) } returns CreditRisk.TooRisky

        val result = assessRisk(created)

        assertThat(result).isEqualTo(
            buildCreditRiskAssessed(
                id = created.id,
                application = created.application,
                creditRisk = CreditRisk.TooRisky
            ).right()
        )
    }

    @Test
    fun `fail coordinating the risk assessment of a loan when customer is not found`() {
        val unevaluatedLoan = buildLoanCreated()
        every { findCustomer(unevaluatedLoan.application.customerId) } returns null

        val result = assessRisk(unevaluatedLoan)

        assertThat(result).isEqualTo(CustomerNotFound(unevaluatedLoan.application.customerId).left())
    }
}
