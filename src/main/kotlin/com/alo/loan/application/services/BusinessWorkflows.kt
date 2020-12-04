package com.alo.loan.application.services

import arrow.core.Either
import com.alo.loan.domain.model.Error
import java.math.BigDecimal
import java.util.UUID

// AKA: Incoming ports in hexagonal architecture

typealias Evaluate = (LoanEvaluationRequest) -> Either<Error, Unit>

data class LoanEvaluationRequest(val id: UUID, val customerId: UUID, val amount: BigDecimal)
