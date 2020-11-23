package com.alo.loan.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right

private val defaultRiskOf = LoanEvaluation.Behaviour::simpleCreditRiskAssessment

class AssessRiskService(
    private val findCustomer: FindCustomer,
    private val getCreditScore: GetCreditScore,
    private val riskOf: (LoanApplication, CreditScore) -> RiskReport = defaultRiskOf
) : AssessCreditRisk {
    override fun invoke(loan: UnevaluatedLoan): Either<CustomerNotFound, RiskAssessed> {
        val customer = findCustomer(loan.application.customerId)?.right()
            ?: CustomerNotFound(loan.application.customerId).left()
        return customer
            .map(getCreditScore)
            .map { riskOf(loan.application, it) }
            .map { RiskAssessed(loan.id, loan.application, it) }
    }
}

private val defaultEligibilityOf = LoanEvaluation.Behaviour::simpleEligibilityAssessment

class AssessEligibilityService(
    private val findCustomer: FindCustomer,
    private val getLoanRecords: GetLoanRecords,
    private val eligibilityOf: (Customer, List<LoanRecord>) -> EligibilityReport = defaultEligibilityOf
) : AssessEligibility {
    override fun invoke(loan: RiskAssessed): Either<Error, EvaluableLoan> {
        val customer = findCustomer(loan.application.customerId)?.right()
            ?: CustomerNotFound(loan.application.customerId).left()
        return customer
            .map { Pair(getLoanRecords(it.id), it) }
            .map { (loanRecords, customer) -> eligibilityOf(customer, loanRecords) }
            .map { eligibility -> EvaluableLoan(loan.id, loan.application, loan.riskReport, eligibility) }
    }
}

// if you prefer with a function, it does not matter, both are functions

//fun assessRiskService(
//    findCustomer: FindCustomer,
//    getCreditScore: GetCreditScore,
//    assess: (LoanApplication, CreditScore) -> RiskReport = defaultAssess
//): AssessCreditRisk = { initialLoan: InitialLoan -> TODO("Not yet implemented") }
