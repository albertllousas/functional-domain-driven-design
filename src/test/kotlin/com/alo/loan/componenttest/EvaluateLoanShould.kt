package com.alo.loan.componenttest

import com.alo.loan.domain.model.LoanApproved
import com.alo.loan.domain.model.evaluation.CreditScore
import com.alo.loan.fixtures.buildCustomer
import com.alo.loan.infrastructure.adapters.incoming.stream.LoanEvaluationRequestEvent
import com.alo.loan.infrastructure.configuration.FakeApp
import com.alo.loan.infrastructure.fake.Event
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class EvaluateLoanShould {

    private val optimalAge = 30
    private val enoughAnnualIncomes = 25_000.toBigDecimal()

    @Test
    @DisplayName(
        """
          GIVEN a request for a loan evaluation
          WHEN all checks pass
          THEN the loan is approved
        """
    )
    fun `return approved scenario`() {
        val testConsumer = TestConsumer()
        val fakeApp = FakeApp(
            requestLoanStream = "stream.request-loan-evaluation-stream",
            resultEvaluationStream = "stream.result-loan-evaluation-stream",
            staticFindCustomerAnswer = buildCustomer(age = optimalAge, annualIncomes = enoughAnnualIncomes),
            staticCreditScoreAnswer = CreditScore.Excellent,
            staticGetLoanRecordsAnswer = emptyList()
        ).also { it.launch() }
        val loanEvaluationRequestEvent =
            LoanEvaluationRequestEvent(id = randomUUID(), customerId = randomUUID(), amount = 5000.toBigDecimal())
        val eventPayload = fakeApp.objectMapper.writeValueAsBytes(loanEvaluationRequestEvent)
        fakeApp.inMemoryFakeEventStream.subscribe("stream.result-loan-evaluation-stream", testConsumer::reactTo)

        fakeApp.inMemoryFakeEventStream.publish(
            event = Event("LoanEvaluationRequestEvent", eventPayload),
            stream = "stream.request-loan-evaluation-stream"
        )

        assertThat(testConsumer.receivedEvents).isEqualTo(
            listOf(
                Event(
                    "LoanApproved",
                    fakeApp.objectMapper.writeValueAsBytes(LoanApproved(loanEvaluationRequestEvent.id))
                )
            )
        )
    }
}

class TestConsumer() {
    val receivedEvents = ArrayList<Event>()
    fun reactTo(event: Event) = receivedEvents.add(event)
}
