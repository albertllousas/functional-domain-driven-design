package com.alo.loan.fixtures

import com.alo.loan.domain.model.AmountToLend
import com.alo.loan.domain.model.Application
import com.alo.loan.domain.model.CustomerCreditRisk
import com.alo.loan.domain.model.CustomerCreditRisk.Low
import com.alo.loan.domain.model.CustomerCreditRisk.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.CustomerCreditRisk.TooRisky
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
import com.alo.loan.domain.model.LoanApplication.Created
import com.alo.loan.domain.model.LoanApplication.CreditRiskAssessed
import com.alo.loan.domain.model.LoanApplication.EligibilityAssessed
import com.alo.loan.domain.model.LoanApplication.Evaluated
import com.alo.loan.domain.model.LoanApplicationId
import com.github.javafaker.Faker
import java.util.UUID

private val faker = Faker()

private fun eligibility() = faker.options().option(Eligible, NonPayer, AlreadyInDebt, NotEnoughAnnualIncomes, InvalidAge)

fun buildApplication(
    customerId: CustomerId = CustomerId(UUID.randomUUID()),
    amountToLend: AmountToLend = AmountToLend(faker.number().randomDigit().toBigDecimal())
) = Application(customerId, amountToLend)

fun buildCreatedLoanApplication(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication()
) = Created(id, application)

fun buildCreditRiskAssessed(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    customerCreditRisk: CustomerCreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired)
) = CreditRiskAssessed(id, application, customerCreditRisk)

fun buildEligibilityAssessed(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    customerCreditRisk: CustomerCreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    customerEligibility: CustomerEligibility = eligibility()
) = EligibilityAssessed(id, application, customerCreditRisk, customerEligibility)

fun buildRejectedLoan(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    customerCreditRisk: CustomerCreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    customerEligibility: CustomerEligibility = eligibility()
) = Evaluated(id, application, customerCreditRisk, customerEligibility, Rejected)

fun buildFurtherVerificationNeededLoan(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    customerCreditRisk: CustomerCreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    customerEligibility: CustomerEligibility = eligibility()
) = Evaluated(id, application, customerCreditRisk, customerEligibility, FurtherVerificationNeeded)

fun buildApprovedLoan(
    id: LoanApplicationId = LoanApplicationId(UUID.randomUUID()),
    application: Application = buildApplication(),
    customerCreditRisk: CustomerCreditRisk = faker.options().option(Low, TooRisky, ManualRiskAssessmentRequired),
    customerEligibility: CustomerEligibility = eligibility()
) = Evaluated(id, application, customerCreditRisk, customerEligibility, Approved)
