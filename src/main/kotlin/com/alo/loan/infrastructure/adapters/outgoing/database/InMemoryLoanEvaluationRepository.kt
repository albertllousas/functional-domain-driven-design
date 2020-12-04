package com.alo.loan.infrastructure.adapters.outgoing.database

import com.alo.loan.domain.model.Loan
import com.alo.loan.domain.model.SaveLoan

class InMemoryLoanEvaluationRepository(private val loanEvaluations: MutableList<Loan> = ArrayList()) {
    val save: SaveLoan = { loanEvaluations.add(it) }
}
