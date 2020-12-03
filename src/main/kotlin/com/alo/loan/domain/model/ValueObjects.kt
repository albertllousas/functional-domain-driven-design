package com.alo.loan.domain.model

import java.math.BigDecimal
import java.util.UUID

data class LoanApplicationId(val value: UUID)

data class AmountToLend(val amount: BigDecimal)

sealed class LoanRecord {
    object Active : LoanRecord()
    object PaidOff : LoanRecord()
    object Unpaid : LoanRecord()
}

data class CustomerId(val value: UUID)

sealed class CreditScore {
    object Bad : CreditScore()
    object Poor : CreditScore()
    object Fair : CreditScore()
    object Good : CreditScore()
    object Excellent : CreditScore()
}
