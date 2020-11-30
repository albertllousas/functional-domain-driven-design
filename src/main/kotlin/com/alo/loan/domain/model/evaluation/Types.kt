package com.alo.loan.domain.model.evaluation

import arrow.core.Either
import com.alo.loan.domain.model.CustomerNotFound
import com.alo.loan.domain.model.DomainEvent
import com.alo.loan.domain.model.Error

// Types required by business workflows
// they will be implemented within the domain, either an function on an aggregate or a domain service

typealias AssessCreditRisk = (UnevaluatedLoan) -> Either<CustomerNotFound, RiskAssessed>

typealias AssessEligibility = (RiskAssessed) -> Either<CustomerNotFound, EligibilityAssessed>

typealias EvaluateLoanApplication = (EligibilityAssessed) -> EvaluatedLoan

typealias CreateEvents = (EvaluatedLoan) -> List<DomainEvent>
