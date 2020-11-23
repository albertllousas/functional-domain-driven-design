package com.alo.loan.infrastructure.adapters.outgoing.database

import com.alo.loan.domain.model.LoanEvaluation
import com.alo.loan.domain.model.SaveLoanEvaluation

class InMemoryLoanEvaluationRepository(private val loanEvaluations: MutableList<LoanEvaluation> = ArrayList()) {
    val save: SaveLoanEvaluation = { loanEvaluations.add(it) }
}
