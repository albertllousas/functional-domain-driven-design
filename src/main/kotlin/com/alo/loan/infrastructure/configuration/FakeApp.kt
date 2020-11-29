package com.alo.loan.infrastructure.configuration

import com.alo.loan.application.services.EvaluateLoan
import com.alo.loan.application.services.evaluateLoanService
import com.alo.loan.domain.model.FindCustomer
import com.alo.loan.domain.model.GetCreditScore
import com.alo.loan.domain.model.GetLoanRecords
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.evaluation.AssessCreditRisk
import com.alo.loan.domain.model.evaluation.AssessEligibility
import com.alo.loan.domain.model.evaluation.AssessEligibilityService
import com.alo.loan.domain.model.evaluation.AssessRiskService
import com.alo.loan.domain.model.evaluation.CreditScore
import com.alo.loan.domain.model.evaluation.Customer
import com.alo.loan.domain.model.evaluation.LoanEvaluation
import com.alo.loan.domain.model.evaluation.LoanRecord
import com.alo.loan.domain.model.evaluation.createEvents
import com.alo.loan.domain.model.evaluation.evaluate
import com.alo.loan.infrastructure.adapters.incoming.stream.InMemoryLoanEvaluationStreamConsumer
import com.alo.loan.infrastructure.adapters.outgoing.client.InMemoryCreditScoreFakeHttpClient
import com.alo.loan.infrastructure.adapters.outgoing.database.InMemoryFakeReplicatedLoanRecordRepository
import com.alo.loan.infrastructure.adapters.outgoing.database.InMemoryFakeReplicatedCustomerRepository
import com.alo.loan.infrastructure.adapters.outgoing.database.InMemoryLoanEvaluationRepository
import com.alo.loan.infrastructure.adapters.outgoing.events.InMemoryDomainEventPublisher
import com.alo.loan.infrastructure.adapters.outgoing.events.InMemoryFakeStreamSubscriber
import com.alo.loan.infrastructure.fake.InMemoryFakeEventStream
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class FakeApp(
    private val loanApplicationStream: String,
    private val evaluationStream: String,
    private val staticFindCustomerAnswer: Customer?,
    private val staticCreditScoreAnswer: CreditScore,
    private val staticGetLoanRecordsAnswer: List<LoanRecord>
) {
    val inMemoryFakeEventStream = InMemoryFakeEventStream()

    val objectMapper = jacksonObjectMapper()

    fun launch() {
        // wire up outbound infrastructure adapters
        val findCustomer: FindCustomer = InMemoryFakeReplicatedCustomerRepository(staticFindCustomerAnswer)
        val getCreditScore: GetCreditScore = InMemoryCreditScoreFakeHttpClient(staticCreditScoreAnswer)
        val getLoanRecords: GetLoanRecords = InMemoryFakeReplicatedLoanRecordRepository(staticGetLoanRecordsAnswer)
        val inMemoryLoanEvaluationRepository = InMemoryLoanEvaluationRepository()
        val domainEventSubscriber =
            InMemoryFakeStreamSubscriber(inMemoryFakeEventStream, objectMapper, evaluationStream)
        val publishEvents: PublishEvents = InMemoryDomainEventPublisher(listOf(domainEventSubscriber))
        // wire up business, the inner hexagon
        val assessRiskService: AssessCreditRisk = AssessRiskService(findCustomer, getCreditScore)
        val assessEligibilityService: AssessEligibility = AssessEligibilityService(findCustomer, getLoanRecords)
        val evaluateLoanService: EvaluateLoan = evaluateLoanService(
            assessCreditRisk = assessRiskService,
            assessEligibility = assessEligibilityService,
            evaluateLoanApplication = LoanEvaluation.Behaviour.evaluate,
            saveLoanEvaluation = inMemoryLoanEvaluationRepository.save,
            createEvents = LoanEvaluation.Behaviour.createEvents,
            publishEvents = publishEvents
        )
        // wire up incoming infrastructure adapters
        val inMemoryLoanEvaluationStreamConsumer =
            InMemoryLoanEvaluationStreamConsumer(evaluateLoanService, objectMapper)

        // subscribe streams
        inMemoryFakeEventStream.subscribe(loanApplicationStream, inMemoryLoanEvaluationStreamConsumer::reactTo)
    }
}
