package com.alo.loan.domain.model

import java.util.UUID

sealed class DomainEvent

data class LoanApplicationRejected(val evaluationId: UUID) : DomainEvent()
data class LoanApplicationHeldForFurtherVerification(val evaluationId: UUID) : DomainEvent()
data class LoanApplicationApproved(val evaluationId: UUID) : DomainEvent()

sealed class Error

data class CustomerNotFound(val customerId: CustomerId) : Error()

