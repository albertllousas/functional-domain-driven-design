package com.alo.loan.infrastructure.adapters.outgoing.database

import com.alo.loan.domain.model.LoanApplication
import com.alo.loan.domain.model.SaveLoanApplication

class InMemoryLoanEvaluationRepository(private val loanEvaluations: MutableList<LoanApplication> = ArrayList()) {
    val save: SaveLoanApplication = { loanEvaluations.add(it) }
}
