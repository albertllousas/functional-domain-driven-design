package com.alo.loan.infrastructure.configuration

import com.alo.loan.application.services.Evaluate
import com.alo.loan.application.services.evaluateService
import com.alo.loan.domain.model.AssessCreditRisk
import com.alo.loan.domain.model.AssessEligibility
import com.alo.loan.domain.model.AssessEligibilityService
import com.alo.loan.domain.model.AssessRiskService
import com.alo.loan.domain.model.CreditScore
import com.alo.loan.domain.model.Customer
import com.alo.loan.domain.model.FindCustomer
import com.alo.loan.domain.model.GetCreditScore
import com.alo.loan.domain.model.GetLoanRecords
import com.alo.loan.domain.model.Loan
import com.alo.loan.domain.model.LoanRecord
import com.alo.loan.domain.model.PublishEvents
import com.alo.loan.domain.model.evaluateAndCreateEvents
import com.alo.loan.infrastructure.adapters.incoming.stream.InMemoryLoanEvaluationStreamConsumer
import com.alo.loan.infrastructure.adapters.outgoing.client.InMemoryCreditScoreFakeHttpClient
import com.alo.loan.infrastructure.adapters.outgoing.database.InMemoryFakeReplicatedCustomerRepository
import com.alo.loan.infrastructure.adapters.outgoing.database.InMemoryFakeReplicatedLoanRecordRepository
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
        val evaluateService: Evaluate = evaluateService(
            assessCreditRisk = assessRiskService,
            assessEligibility = assessEligibilityService,
            evaluateLoan = Loan.evaluateAndCreateEvents,
            saveLoan = inMemoryLoanEvaluationRepository.save,
            publishEvents = publishEvents
        )
        // wire up incoming infrastructure adapters
        val inMemoryLoanEvaluationStreamConsumer =
            InMemoryLoanEvaluationStreamConsumer(evaluateService, objectMapper)

        // subscribe streams
        inMemoryFakeEventStream.subscribe(loanApplicationStream, inMemoryLoanEvaluationStreamConsumer::reactTo)
    }
}
