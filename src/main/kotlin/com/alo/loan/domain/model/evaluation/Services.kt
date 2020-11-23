package com.alo.loan.domain.model.evaluation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.alo.loan.domain.model.CustomerNotFound
import com.alo.loan.domain.model.Error
import com.alo.loan.domain.model.FindCustomer
import com.alo.loan.domain.model.GetCreditScore
import com.alo.loan.domain.model.GetLoanRecords

// Domain Services

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
