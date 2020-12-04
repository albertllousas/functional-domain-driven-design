package com.alo.loan.domain.model

import com.alo.loan.domain.model.CustomerEligibility.Eligible
import com.alo.loan.domain.model.CustomerEligibility.ManualEligibilityAssessmentRequired
import com.alo.loan.domain.model.CustomerEligibility.NotEligible
import java.math.BigDecimal

data class Customer(
    val id: CustomerId,
    val fullName: String,
    val address: String,
    val age: Int,
    val annualIncomes: BigDecimal
) {
    companion object
}

sealed class CustomerEligibility {
    object Eligible : CustomerEligibility()
    object ManualEligibilityAssessmentRequired : CustomerEligibility()
    sealed class NotEligible : CustomerEligibility() {
        object InvalidAge : NotEligible()
        object NotEnoughAnnualIncomes : NotEligible()
        object NonPayer : NotEligible()
        object AlreadyInDebt : NotEligible()
    }
}

// simple implementation, for real production code you could have different implementations, as complex as business requires
fun Customer.Companion.eligibilityOf(
    customer: Customer,
    loanRecords: List<LoanRecord>
): CustomerEligibility =
    when {
        customer.age in 18..21 && customer.annualIncomes >= 30000.toBigDecimal() -> ManualEligibilityAssessmentRequired
        customer.age !in 21..60 -> NotEligible.InvalidAge
        customer.annualIncomes < 20000.toBigDecimal() -> NotEligible.NotEnoughAnnualIncomes
        loanRecords.contains(LoanRecord.Unpaid) -> NotEligible.NonPayer
        loanRecords.contains(LoanRecord.Active) -> NotEligible.AlreadyInDebt
        else -> Eligible
    }
