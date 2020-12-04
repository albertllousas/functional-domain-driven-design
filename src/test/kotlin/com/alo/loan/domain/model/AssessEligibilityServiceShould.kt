package com.alo.loan.domain.model

import arrow.core.left
import arrow.core.right
import com.alo.loan.fixtures.buildCreditRiskAssessed
import com.alo.loan.fixtures.buildCustomer
import com.alo.loan.fixtures.buildEligibilityAssessed
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AssessEligibilityServiceShould {

    private val findCustomer = mockk<FindCustomer>()

    private val getLoanRecords = mockk<GetLoanRecords>()

    private val eligibilityOf = mockk<(Customer, List<LoanRecord>) -> CustomerEligibility>(relaxed = true)

    private val assessEligibility = AssessEligibilityService(findCustomer, getLoanRecords, eligibilityOf)

    @Test
    fun `coordinate the eligibility assessment of a loan with external world dependencies`() {
        val riskAssessed = buildCreditRiskAssessed()
        val customer = buildCustomer()
        every { findCustomer(riskAssessed.application.customerId) } returns customer
        every { getLoanRecords(customer.id) } returns listOf(LoanRecord.PaidOff)
        every { eligibilityOf(customer, listOf(LoanRecord.PaidOff)) } returns CustomerEligibility.Eligible

        val result = assessEligibility(riskAssessed)

        assertThat(result).isEqualTo(
            buildEligibilityAssessed(
                id = riskAssessed.id,
                application = riskAssessed.application,
                creditRisk = riskAssessed.creditRisk,
                customerEligibility = CustomerEligibility.Eligible
            ).right()
        )
    }

    @Test
    fun `fail coordinating eligibility assessment of a loan when customer is not found`() {
        val riskAssessed = buildCreditRiskAssessed()
        every { findCustomer(riskAssessed.application.customerId) } returns null

        val result = assessEligibility(riskAssessed)

        assertThat(result).isEqualTo(CustomerNotFound(riskAssessed.application.customerId).left())
    }
}
