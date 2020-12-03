package com.alo.loan.domain.model

import com.alo.loan.domain.model.CreditScore.Bad
import com.alo.loan.domain.model.CreditScore.Excellent
import com.alo.loan.domain.model.CreditScore.Fair
import com.alo.loan.domain.model.CreditScore.Good
import com.alo.loan.domain.model.CreditScore.Poor
import com.alo.loan.domain.model.CustomerCreditRisk.Low
import com.alo.loan.domain.model.CustomerCreditRisk.ManualRiskAssessmentRequired
import com.alo.loan.domain.model.CustomerCreditRisk.TooRisky
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

sealed class CustomerCreditRisk {
    object Low : CustomerCreditRisk()
    object TooRisky : CustomerCreditRisk()
    object ManualRiskAssessmentRequired : CustomerCreditRisk()
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
fun Customer.Companion.creditRiskOf(
    amountToLend: AmountToLend,
    creditScore: CreditScore
): CustomerCreditRisk =
    when (creditScore) {
        is Bad, is Poor -> TooRisky
        is Fair -> if (amountToLend.amount > 1000.toBigDecimal()) TooRisky else Low
        is Good -> if (amountToLend.amount > 5000.toBigDecimal()) TooRisky else Low
        is Excellent -> if (amountToLend.amount > 10000.toBigDecimal()) ManualRiskAssessmentRequired else Low
    }

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
