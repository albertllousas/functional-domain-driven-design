package com.alo.loan.domain.model

import java.util.UUID

sealed class DomainEvent

data class LoanRejected(val evaluationId: UUID, val reasons: List<String>) : DomainEvent()
data class LoanHeldForFurtherVerification(val evaluationId: UUID) : DomainEvent()
data class LoanApproved(val evaluationId: UUID) : DomainEvent()
