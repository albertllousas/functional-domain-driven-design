package com.alo.loan.domain.model

// AKA: Outgoing ports in hexagonal architecture - They will be implemented as adapters in the infrastructure layer

typealias FindCustomer = (CustomerId) -> Customer?

typealias GetLoanRecords = (CustomerId) -> List<LoanRecord>

typealias GetCreditScore = (Customer) -> CreditScore

typealias SaveLoan = (Loan.Evaluated) -> Unit

typealias PublishEvents = (List<DomainEvent>) -> Unit
