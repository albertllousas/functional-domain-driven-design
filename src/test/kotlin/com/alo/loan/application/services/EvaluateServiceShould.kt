package com.alo.loan.application.services

import arrow.core.left
import arrow.core.right
import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.AssessCreditRisk
import com.alo.loan.domain.model.AssessEligibility
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.CustomerNotFound
import com.alo.loan.domain.model.EvaluateLoanApplication
import com.alo.loan.domain.model.LoanApplicationApproved
import com.alo.loan.domain.model.LoanApplicationId
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.SaveLoanApplication
import com.alo.loan.fixtures.buildApplication
import com.alo.loan.fixtures.buildApprovedLoan
import com.alo.loan.fixtures.buildCreatedLoanApplication
import com.alo.loan.fixtures.buildCreditRiskAssessed
import com.alo.loan.fixtures.buildEligibilityAssessed
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class EvaluateServiceShould {

    private val assessCreditRisk = mockk<AssessCreditRisk>(relaxed = true)
    private val assessEligibility = mockk<AssessEligibility>(relaxed = true)
    private val evaluateLoanApplication = mockk<EvaluateLoanApplication>(relaxed = true)
    private val saveLoanEvaluationReport = mockk<SaveLoanApplication>(relaxed = true)
    private val publishEvents = mockk<PublishEvents>(relaxed = true)
    private val evaluate: Evaluate = evaluateService(
        assessCreditRisk,
        assessEligibility,
        evaluateLoanApplication,
        saveLoanEvaluationReport,
        publishEvents
    )

    @Test
    fun `evaluate a loan`() {
        val request = LoanApplicationEvaluationRequest(
            id = randomUUID(),
            customerId = randomUUID(),
            amount = 15000.toBigDecimal()
        )
        val application = buildApplication(CustomerId(request.customerId), AmountToLend(request.amount))
        val created = buildCreatedLoanApplication(LoanApplicationId(request.id), application)
        val riskAssessedLoan = buildCreditRiskAssessed()
        val evaluableLoan = buildEligibilityAssessed()
        val approvedLoan = buildApprovedLoan()
        every { assessCreditRisk(created) } returns riskAssessedLoan.right()
        every { assessEligibility(riskAssessedLoan) } returns evaluableLoan.right()
        every { evaluateLoanApplication(evaluableLoan) } returns Pair(approvedLoan, listOf(LoanApplicationApproved(request.id)))
        every { publishEvents(listOf(LoanApplicationApproved(request.id))) } returns Unit

        val result = evaluate(request)

        assertThat(result).isEqualTo(Unit.right())
        verify { saveLoanEvaluationReport(approvedLoan) }
    }

    @Test
    fun `fail evaluating a loan when any of the steps fail`() {
        val request = LoanApplicationEvaluationRequest(
            id = randomUUID(),
            customerId = randomUUID(),
            amount = 15000.toBigDecimal()
        )
        val loanApplication = buildApplication(CustomerId(request.customerId), AmountToLend(request.amount))
        val unevaluatedLoan = buildCreatedLoanApplication(LoanApplicationId(request.id), loanApplication)
        val customerNotFound = CustomerNotFound(unevaluatedLoan.application.customerId)
        every { assessCreditRisk(unevaluatedLoan) } returns customerNotFound.left()

        val result = evaluate(request)

        assertThat(result).isEqualTo(customerNotFound.left())
    }
}
