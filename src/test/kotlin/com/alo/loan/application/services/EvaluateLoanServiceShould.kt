package com.alo.loan.application.services

import arrow.core.left
import arrow.core.right
import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.AssessCreditRisk
import com.alo.loan.domain.model.AssessEligibility
import com.alo.loan.domain.model.CreateEvents
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.CustomerNotFound
import com.alo.loan.domain.model.EvaluateLoanApplication
import com.alo.loan.domain.model.EvaluationId
import com.alo.loan.domain.model.LoanApproved
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.SaveLoanEvaluation
import com.alo.loan.fixtures.buildApprovedLoan
import com.alo.loan.fixtures.buildEvaluableLoan
import com.alo.loan.fixtures.buildLoanApplication
import com.alo.loan.fixtures.buildRiskAssessedLoan
import com.alo.loan.fixtures.buildUnevaluatedLoan
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class EvaluateLoanServiceShould {

    private val assessCreditRisk = mockk<AssessCreditRisk>(relaxed = true)
    private val assessEligibility = mockk<AssessEligibility>(relaxed = true)
    private val evaluateLoanApplication = mockk<EvaluateLoanApplication>(relaxed = true)
    private val saveLoanEvaluationReport = mockk<SaveLoanEvaluation>(relaxed = true)
    private val createEvents = mockk<CreateEvents>(relaxed = true)
    private val publishEvents = mockk<PublishEvents>(relaxed = true)
    private val evaluateLoan: EvaluateLoan = evaluateLoanService(
        assessCreditRisk,
        assessEligibility,
        evaluateLoanApplication,
        saveLoanEvaluationReport,
        createEvents,
        publishEvents
    )

    @Test
    fun `evaluate a loan`() {
        val request = LoanEvaluationRequest(
            id = randomUUID(),
            customerId = randomUUID(),
            amount = 15000.toBigDecimal()
        )
        val loanApplication = buildLoanApplication(CustomerId(request.customerId), AmountToLend(request.amount))
        val unevaluatedLoan = buildUnevaluatedLoan(EvaluationId(request.id), loanApplication)
        val riskAssessedLoan = buildRiskAssessedLoan()
        val evaluableLoan = buildEvaluableLoan()
        val approvedLoan = buildApprovedLoan()
        every { assessCreditRisk(unevaluatedLoan) } returns riskAssessedLoan.right()
        every { assessEligibility(riskAssessedLoan) } returns evaluableLoan.right()
        every { evaluateLoanApplication(evaluableLoan) } returns approvedLoan
        every { createEvents(approvedLoan) } returns listOf(LoanApproved(request.id))
        every { publishEvents(listOf(LoanApproved(request.id))) } returns Unit

        val result = evaluateLoan(request)

        assertThat(result).isEqualTo(Unit.right())
        verify { saveLoanEvaluationReport(approvedLoan) }
    }

    @Test
    fun `fail evaluating a loan when any of the steps fail`() {
        val request = LoanEvaluationRequest(
            id = randomUUID(),
            customerId = randomUUID(),
            amount = 15000.toBigDecimal()
        )
        val loanApplication = buildLoanApplication(CustomerId(request.customerId), AmountToLend(request.amount))
        val unevaluatedLoan = buildUnevaluatedLoan(EvaluationId(request.id), loanApplication)
        val customerNotFound = CustomerNotFound(unevaluatedLoan.application.customerId)
        every { assessCreditRisk(unevaluatedLoan) } returns customerNotFound.left()

        val result = evaluateLoan(request)

        assertThat(result).isEqualTo(customerNotFound.left())
    }
}
