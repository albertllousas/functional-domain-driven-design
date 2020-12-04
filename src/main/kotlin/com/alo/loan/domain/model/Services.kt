package com.alo.loan.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.alo.loan.domain.model.Loan.Created
import com.alo.loan.domain.model.Loan.CreditRiskAssessed
import com.alo.loan.domain.model.Loan.CustomerEligibilityAssessed

// Domain Services

private val defaultCreditRiskOf = Loan.Companion::creditRiskOf
// If you prefer you can create a fun that implements the contract, it does not matter both are functions
class AssessRiskService(
    private val findCustomer: FindCustomer,
    private val getCreditScore: GetCreditScore,
    private val creditRiskOf: (AmountToLend, CreditScore) -> CreditRisk = defaultCreditRiskOf
) : AssessCreditRisk {
    override fun invoke(loan: Created): Either<CustomerNotFound, CreditRiskAssessed> {
        val customer = findCustomer(loan.application.customerId)?.right()
            ?: CustomerNotFound(loan.application.customerId).left()
        return customer
            .map(getCreditScore)
            .map { creditRiskOf(loan.application.amountToLend, it) }
            .map { CreditRiskAssessed(loan.id, loan.application, it) }
    }
}

private val defaultEligibilityOf = Customer.Companion::eligibilityOf

class AssessEligibilityService(
    private val findCustomer: FindCustomer,
    private val getLoanRecords: GetLoanRecords,
    private val customerEligibilityOf: (Customer, List<LoanRecord>) -> CustomerEligibility = defaultEligibilityOf
) : AssessEligibility {
    override fun invoke(loan: CreditRiskAssessed): Either<CustomerNotFound, CustomerEligibilityAssessed> {
        val customer = findCustomer(loan.application.customerId)?.right()
            ?: CustomerNotFound(loan.application.customerId).left()
        return customer
            .map { Pair(getLoanRecords(it.id), it) }
            .map { (loanRecords, customer) -> customerEligibilityOf(customer, loanRecords) }
            .map { eligibility -> CustomerEligibilityAssessed(loan.id, loan.application, loan.creditRisk, eligibility) }
    }
}
