package com.alo.loan.domain.model

import arrow.core.Either

// Functions required by business workflows
// they will be implemented within the domain, either an function on an aggregate or a domain service

typealias AssessCreditRisk = (UnevaluatedLoan) -> Either<Error, RiskAssessed>

typealias AssessEligibility = (RiskAssessed) -> Either<Error, EvaluableLoan>

typealias EvaluateLoanApplication = (EvaluableLoan) -> EvaluatedLoan

typealias CreateEvents = (EvaluatedLoan) -> List<DomainEvent>
