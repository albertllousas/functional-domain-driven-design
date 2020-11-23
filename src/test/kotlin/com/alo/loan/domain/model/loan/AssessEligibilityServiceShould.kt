package com.alo.loan.domain.model.loan

import arrow.core.left
import arrow.core.right
import com.alo.loan.domain.model.CustomerNotFound
import com.alo.loan.domain.model.FindCustomer
import com.alo.loan.domain.model.GetLoanRecords
import com.alo.loan.domain.model.evaluation.AssessEligibilityService
import com.alo.loan.domain.model.evaluation.Customer
import com.alo.loan.domain.model.evaluation.EligibilityReport
import com.alo.loan.domain.model.evaluation.EligibilityReport.Eligible
import com.alo.loan.domain.model.evaluation.LoanRecord
import com.alo.loan.domain.model.evaluation.LoanRecord.PaidOff
import com.alo.loan.fixtures.buildCustomer
import com.alo.loan.fixtures.buildEvaluableLoan
import com.alo.loan.fixtures.buildRiskAssessedLoan
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AssessEligibilityServiceShould {

    private val findCustomer = mockk<FindCustomer>()

    private val getLoanRecords = mockk<GetLoanRecords>()

    private val eligibilityOf = mockk<(Customer, List<LoanRecord>) -> EligibilityReport>(relaxed = true)

    private val assessEligibility = AssessEligibilityService(findCustomer, getLoanRecords, eligibilityOf)

    @Test
    fun `coordinate the eligibility assessment of a loan with external world dependencies`() {
        val riskAssessed = buildRiskAssessedLoan()
        val customer = buildCustomer()
        every { findCustomer(riskAssessed.application.customerId) } returns customer
        every { getLoanRecords(customer.id) } returns listOf(PaidOff)
        every { eligibilityOf(customer, listOf(PaidOff)) } returns Eligible

        val result = assessEligibility(riskAssessed)

        assertThat(result).isEqualTo(
            buildEvaluableLoan(
                id = riskAssessed.id,
                application = riskAssessed.application,
                riskReport = riskAssessed.riskReport,
                eligibilityReport = Eligible
            ).right()
        )
    }

    @Test
    fun `fail coordinating eligibility assessment of a loan when customer is not found`() {
        val riskAssessed = buildRiskAssessedLoan()
        every { findCustomer(riskAssessed.application.customerId) } returns null

        val result = assessEligibility(riskAssessed)

        assertThat(result).isEqualTo(CustomerNotFound(riskAssessed.application.customerId).left())
    }
}
