package com.alo.loan.domain.model

import arrow.core.Either

// Types required by business workflows
// they will be implemented within the domain, either a function on an aggregate or a domain service

typealias AssessCreditRisk = (LoanApplication.Created) -> Either<Error, LoanApplication.CreditRiskAssessed>

typealias AssessEligibility = (LoanApplication.CreditRiskAssessed) -> Either<CustomerNotFound, LoanApplication.EligibilityAssessed>

typealias EvaluateLoanApplication = (LoanApplication.EligibilityAssessed) -> Pair<LoanApplication.Evaluated, List<DomainEvent>>
