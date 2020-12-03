package com.alo.loan.infrastructure.adapters.incoming.stream

import com.alo.loan.application.services.Evaluate
import com.alo.loan.application.services.LoanApplicationEvaluationRequest
import com.alo.loan.infrastructure.fake.Event
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.util.UUID

class InMemoryLoanEvaluationStreamConsumer(
    private val evaluate: Evaluate,
    private val objectMapper: ObjectMapper
) {
    // omitting all error handling since this is a fake inmemory impl for the sake of the demo
    fun reactTo(event: Event) =
        if (event.eventType == "LoanApplicationCreatedEvent")
            objectMapper
                .readValue<LoanApplicationCreatedEvent>(event.eventPayload)
                .let { LoanApplicationEvaluationRequest(it.id, it.customerId, it.amount) }
                .also { evaluate(it) }
        else throw Exception("Not recognized event '${event.eventType}'")
}

data class LoanApplicationCreatedEvent(val id: UUID, val customerId: UUID, val amount: BigDecimal)
