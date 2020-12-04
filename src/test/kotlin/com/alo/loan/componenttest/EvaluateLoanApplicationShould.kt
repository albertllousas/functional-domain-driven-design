package com.alo.loan.componenttest

import com.alo.loan.domain.model.CreditScore
import com.alo.loan.domain.model.LoanApplicationApproved
import com.alo.loan.fixtures.buildCustomer
import com.alo.loan.infrastructure.adapters.incoming.stream.LoanApplicationCreatedEvent
import com.alo.loan.infrastructure.configuration.FakeApp
import com.alo.loan.infrastructure.fake.Event
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class EvaluateLoanApplicationShould {

    private val optimalAge = 30
    private val enoughAnnualIncomes = 25_000.toBigDecimal()

    @Test
    @DisplayName(
        """
          GIVEN a loan application is created
          WHEN all checks pass
          THEN the loan is approved
        """
    )
    fun `return approved scenario`() {
        val testConsumer = TestConsumer()
        val fakeApp = FakeApp(
            loanApplicationStream = "stream.loan-application",
            evaluationStream = "stream.loan-evaluation-stream",
            staticFindCustomerAnswer = buildCustomer(age = optimalAge, annualIncomes = enoughAnnualIncomes),
            staticCreditScoreAnswer = CreditScore.Excellent,
            staticGetLoanRecordsAnswer = emptyList()
        ).also { it.launch() }
        val applicationCreatedEvent =
            LoanApplicationCreatedEvent(id = randomUUID(), customerId = randomUUID(), amount = 5000.toBigDecimal())
        val eventPayload = fakeApp.objectMapper.writeValueAsBytes(applicationCreatedEvent)
        fakeApp.inMemoryFakeEventStream.subscribe("stream.loan-evaluation-stream", testConsumer::reactTo)

        fakeApp.inMemoryFakeEventStream.publish(
            event = Event("LoanApplicationCreatedEvent", eventPayload),
            stream = "stream.loan-application"
        )

        assertThat(testConsumer.receivedEvents).isEqualTo(
            listOf(
                Event(
                    "LoanApproved",
                    fakeApp.objectMapper.writeValueAsBytes(LoanApplicationApproved(applicationCreatedEvent.id))
                )
            )
        )
    }
}

class TestConsumer() {
    val receivedEvents = ArrayList<Event>()
    fun reactTo(event: Event) = receivedEvents.add(event)
}
