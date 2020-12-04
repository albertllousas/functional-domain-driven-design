package com.alo.loan.domain.model

import arrow.core.Either

// Types required by business workflows
// they will be implemented within the domain, either a function on an aggregate or a domain service

typealias AssessCreditRisk = (Loan.Created) -> Either<CustomerNotFound, Loan.CreditRiskAssessed>

typealias AssessEligibility = (Loan.CreditRiskAssessed) -> Either<CustomerNotFound, Loan.CustomerEligibilityAssessed>

typealias EvaluateLoan = (Loan.CustomerEligibilityAssessed) -> Pair<Loan.Evaluated, List<DomainEvent>>
