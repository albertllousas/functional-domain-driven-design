package com.alo.loan.domain.model.evaluation

import java.math.BigDecimal
import java.util.UUID

// Algebraic data types for LoanEvaluation Aggregate

sealed class LoanEvaluation {
    companion object Behaviour
}

data class UnevaluatedLoan(val id: EvaluationId, val application: LoanApplication) : LoanEvaluation()

data class RiskAssessed(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport
) : LoanEvaluation()

data class EligibilityAssessed(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport,
    val eligibilityReport: EligibilityReport
) : LoanEvaluation()

sealed class EvaluatedLoan : LoanEvaluation()

data class Rejected(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport,
    val eligibilityReport: EligibilityReport,
    val reasons: List<String>
) : EvaluatedLoan()

data class FurtherVerificationNeeded(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport,
    val eligibilityReport: EligibilityReport
) : EvaluatedLoan()

data class Approved(
    val id: EvaluationId,
    val application: LoanApplication,
    val riskReport: RiskReport,
    val eligibilityReport: EligibilityReport
) : EvaluatedLoan()

// Entities & Value Objects

data class LoanApplication(
    val customerId: CustomerId,
    val amountToLend: AmountToLend
)

sealed class RiskReport {
    object Low : RiskReport()
    object TooRisky : RiskReport()
    object ManualRiskAssessmentRequired : RiskReport()
}

sealed class EligibilityReport {
    object Eligible : EligibilityReport()
    object ManualEligibilityAssessmentRequired : EligibilityReport()
    sealed class NotEligible : EligibilityReport() {
        object InvalidAge : NotEligible()
        object NotEnoughAnnualIncomes : NotEligible()
        object NonPayer : NotEligible()
        object AlreadyInDebt : NotEligible()
    }
}

data class EvaluationId(val value: UUID)

data class AmountToLend(val amount: BigDecimal)

// Other entities and value objects required by this aggregate,
// They could become a new module with new aggregates if they grow

sealed class LoanRecord {
    object Active : LoanRecord()
    object PaidOff : LoanRecord()
    object Unpaid : LoanRecord()
}

data class CustomerId(val value: UUID)

data class Customer(
    val id: CustomerId,
    val fullName: String,
    val address: String,
    val age: Int,
    val annualIncomes: BigDecimal
)

sealed class CreditScore {
    object Bad : CreditScore()
    object Poor : CreditScore()
    object Fair : CreditScore()
    object Good : CreditScore()
    object Excellent : CreditScore()
}
