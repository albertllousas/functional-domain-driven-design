package com.alo.loan.domain.model

import arrow.core.left
import arrow.core.right
import com.alo.loan.domain.model.RiskReport.*
import com.alo.loan.fixtures.buildCustomer
import com.alo.loan.fixtures.buildRiskAssessedLoan
import com.alo.loan.fixtures.buildUnevaluatedLoan
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class AssessRiskServiceShould {

    private val findCustomer = mockk<FindCustomer>()

    private val getCreditScore = mockk<GetCreditScore>(relaxed = true)

    private val riskOf = mockk<(LoanApplication, CreditScore) -> RiskReport>(relaxed = true)

    private val assessRisk = AssessRiskService(findCustomer, getCreditScore, riskOf)

    @Test
    fun `coordinate the risk assessment of a loan with external world dependencies`() {
        val unevaluatedLoan = buildUnevaluatedLoan()
        val customer = buildCustomer()
        val creditScore = CreditScore.Poor
        every { findCustomer(unevaluatedLoan.application.customerId) } returns customer
        every { getCreditScore(customer) } returns creditScore
        every { riskOf(unevaluatedLoan.application, creditScore) } returns TooRisky

        val result = assessRisk(unevaluatedLoan)

        assertThat(result).isEqualTo(
            buildRiskAssessedLoan(
                id = unevaluatedLoan.id,
                application = unevaluatedLoan.application,
                riskReport = TooRisky
            ).right()
        )
    }

    @Test
    fun `fail coordinating the risk assessment of a loan when customer is not found`() {
        val unevaluatedLoan = buildUnevaluatedLoan()
        every { findCustomer(unevaluatedLoan.application.customerId) } returns null

        val result = assessRisk(unevaluatedLoan)

        assertThat(result).isEqualTo(CustomerNotFound(unevaluatedLoan.application.customerId).left())
    }
}
