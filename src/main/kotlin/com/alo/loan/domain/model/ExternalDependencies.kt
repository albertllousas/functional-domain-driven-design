package com.alo.loan.domain.model

import com.alo.loan.domain.model.evaluation.CreditScore
import com.alo.loan.domain.model.evaluation.Customer
import com.alo.loan.domain.model.evaluation.CustomerId
import com.alo.loan.domain.model.evaluation.EvaluatedLoan
import com.alo.loan.domain.model.evaluation.LoanRecord

// AKA: Outgoing ports in hexagonal architecture - They will be implemented as adapters in the infrastructure layer

typealias SaveLoanEvaluation = (EvaluatedLoan) -> Unit

typealias FindCustomer = (CustomerId) -> Customer?

typealias GetLoanRecords = (CustomerId) -> List<LoanRecord>

typealias GetCreditScore = (Customer) -> CreditScore

typealias PublishEvents = (List<DomainEvent>) -> Unit
