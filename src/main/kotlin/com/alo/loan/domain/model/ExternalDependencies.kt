package com.alo.loan.domain.model

import java.math.BigDecimal
import java.util.UUID

// AKA: Outgoing ports in hexagonal architecture - They will be implemented as adapters in the infrastructure layer

typealias SaveLoanEvaluation = (EvaluatedLoan) -> Unit

typealias FindCustomer = (CustomerId) -> Customer?

typealias GetLoanRecords = (CustomerId) -> List<LoanRecord>

typealias GetCreditScore = (Customer) -> CreditScore

typealias PublishEvents = (List<DomainEvent>) -> Unit
