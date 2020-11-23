package com.alo.loan.fixtures

import com.alo.loan.domain.model.evaluation.AmountToLend
import com.alo.loan.domain.model.evaluation.Approved
import com.alo.loan.domain.model.evaluation.CustomerId
import com.alo.loan.domain.model.evaluation.EligibilityReport
import com.alo.loan.domain.model.evaluation.EligibilityReport.Eligible
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.AlreadyInDebt
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.InvalidAge
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.NonPayer
import com.alo.loan.domain.model.evaluation.EligibilityReport.NotEligible.NotEnoughAnnualIncomes
import com.alo.loan.domain.model.evaluation.EvaluableLoan
import com.alo.loan.domain.model.evaluation.EvaluationId
import com.alo.loan.domain.model.evaluation.FurtherVerificationNeeded
import com.alo.loan.domain.model.evaluation.LoanApplication
import com.alo.loan.domain.model.evaluation.Rejected
import com.alo.loan.domain.model.evaluation.RiskAssessed
import com.alo.loan.domain.model.evaluation.RiskReport
import com.alo.loan.domain.model.evaluation.RiskReport.Low
import com.alo.loan.domain.model.evaluation.RiskReport.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.evaluation.RiskReport.TooRisky
import com.alo.loan.domain.model.evaluation.UnevaluatedLoan
import com.github.javafaker.Faker
import java.util.UUID

private val faker = Faker()

private fun Faker.eligibilityReport() = faker.options().option(Eligible, NonPayer, AlreadyInDebt, NotEnoughAnnualIncomes, InvalidAge)

fun buildLoanApplication(
    customerId: CustomerId = CustomerId(UUID.randomUUID()),
    amountToLend: AmountToLend = AmountToLend(faker.number().randomDigit().toBigDecimal())
) = LoanApplication(customerId, amountToLend)

fun buildUnevaluatedLoan(
    id: EvaluationId = EvaluationId(UUID.randomUUID()),
    application: LoanApplication = buildLoanApplication()
) = UnevaluatedLoan(id, application)

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
