package com.alo.loan.fixtures

import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.Application
import com.alo.loan.domain.model.CreditRisk
import com.alo.loan.domain.model.CreditRisk.Low
import com.alo.loan.domain.model.CreditRisk.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.CreditRisk.TooRisky
import com.alo.loan.domain.model.CustomerEligibility
import com.alo.loan.domain.model.CustomerEligibility.Eligible
import com.alo.loan.domain.model.CustomerEligibility.NotEligible.AlreadyInDebt
import com.alo.loan.domain.model.CustomerEligibility.NotEligible.InvalidAge
import com.alo.loan.domain.model.CustomerEligibility.NotEligible.NonPayer
import com.alo.loan.domain.model.CustomerEligibility.NotEligible.NotEnoughAnnualIncomes
import com.alo.loan.domain.model.CustomerId
import com.alo.loan.domain.model.Evaluation.Approved
import com.alo.loan.domain.model.Evaluation.FurtherVerificationNeeded
import com.alo.loan.domain.model.Evaluation.Rejected
import com.alo.loan.domain.model.Loan.Created
import com.alo.loan.domain.model.Loan.CreditRiskAssessed
import com.alo.loan.domain.model.Loan.CustomerEligibilityAssessed
import com.alo.loan.domain.model.Loan.Evaluated
import com.alo.loan.domain.model.LoanApplicationId
import com.github.javafaker.Faker
import java.util.UUID

private val faker = Faker()

private fun eligibility() = faker.options().option(Eligible, NonPayer, AlreadyInDebt, NotEnoughAnnualIncomes, InvalidAge)

fun buildApplication(
    customerId: CustomerId = CustomerId(UUID.randomUUID()),
    amountToLend: AmountToLend = AmountToLend(faker.number().randomDigit().toBigDecimal())
) = Application(customerId, amountToLend)

fun buildLoanCreated(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication()
) = Created(id, application)

fun buildCreditRiskAssessed(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    creditRisk: CreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired)
) = CreditRiskAssessed(id, application, creditRisk)

fun buildEligibilityAssessed(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    creditRisk: CreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    customerEligibility: CustomerEligibility = eligibility()
) = CustomerEligibilityAssessed(id, application, creditRisk, customerEligibility)

fun buildRejectedLoan(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    creditRisk: CreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    customerEligibility: CustomerEligibility = eligibility()
) = Evaluated(id, application, creditRisk, customerEligibility, Rejected)

fun buildFurtherVerificationNeededLoan(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    creditRisk: CreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    customerEligibility: CustomerEligibility = eligibility()
) = Evaluated(id, application, creditRisk, customerEligibility, FurtherVerificationNeeded)

fun buildApprovedLoan(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    creditRisk: CreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    customerEligibility: CustomerEligibility = eligibility()
) = Evaluated(id, application, creditRisk, customerEligibility, Approved)
