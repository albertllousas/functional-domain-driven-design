package com.alo.loan.fixtures

import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.Approved
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.EligibilityReport
import com.alo.loan.domain.model.EligibilityReport.Eligible
import com.alo.loan.domain.model.EligibilityReport.NotEligible
import com.alo.loan.domain.model.EligibilityReport.NotEligible.*
import com.alo.loan.domain.model.EvaluableLoan
import com.alo.loan.domain.model.EvaluationId
import com.alo.loan.domain.model.FurtherVerificationNeeded
import com.alo.loan.domain.model.LoanApplication
import com.alo.loan.domain.model.Rejected
import com.alo.loan.domain.model.RiskAssessed
import com.alo.loan.domain.model.RiskReport
import com.alo.loan.domain.model.RiskReport.Low
import com.alo.loan.domain.model.RiskReport.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.RiskReport.TooRisky
import com.alo.loan.domain.model.UnevaluatedLoan
import com.github.javafaker.Faker
import java.util.UUID

private val faker = Faker()

private fun Faker.eligibilityReport() = faker.options().option(Eligible,NonPayer, AlreadyInDebt, NotEnoughAnnualIncomes, InvalidAge)

fun buildLoanApplication(
    customerId: CustomerId = CustomerId(UUID.randomUUID()),
    amountToLend: AmountToLend = AmountToLend(faker.number().randomDigit().toBigDecimal())
) = LoanApplication(customerId, amountToLend)

fun buildUnevaluatedLoan(
    id: EvaluationId = EvaluationId(UUID.randomUUID()),
    application: LoanApplication = buildLoanApplication()) =
    UnevaluatedLoan(id, application)

fun buildRiskAssessedLoan(
    id: EvaluationId = EvaluationId(UUID.randomUUID()),
    application: LoanApplication = buildLoanApplication(),
    riskReport: RiskReport = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired)
) = RiskAssessed(id, application, riskReport)

fun buildEvaluableLoan(
    id: EvaluationId = EvaluationId(UUID.randomUUID()),
    application: LoanApplication = buildLoanApplication(),
    riskReport: RiskReport = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    eligibilityReport: EligibilityReport = faker.eligibilityReport()
) = EvaluableLoan(id, application, riskReport, eligibilityReport)

fun buildRejectedLoan(
    id: EvaluationId = EvaluationId(UUID.randomUUID()),
    application: LoanApplication = buildLoanApplication(),
    riskReport: RiskReport = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    eligibilityReport: EligibilityReport = faker.eligibilityReport(),
    reasons: List<String> = listOf(faker.lorem().sentence())
) = Rejected(id, application, riskReport, eligibilityReport, reasons)

fun buildFurtherVerificationNeededLoan(
    id: EvaluationId = EvaluationId(UUID.randomUUID()),
    application: LoanApplication = buildLoanApplication(),
    riskReport: RiskReport = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    eligibilityReport: EligibilityReport = faker.eligibilityReport()
) = FurtherVerificationNeeded(id, application, riskReport, eligibilityReport)

fun buildApprovedLoan(
    id: EvaluationId = EvaluationId(UUID.randomUUID()),
    application: LoanApplication = buildLoanApplication(),
    riskReport: RiskReport = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    eligibilityReport: EligibilityReport = faker.eligibilityReport()
) = Approved(id, application, riskReport, eligibilityReport)
