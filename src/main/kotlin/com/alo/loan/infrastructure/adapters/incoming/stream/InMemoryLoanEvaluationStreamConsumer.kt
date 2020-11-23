package com.alo.loan.infrastructure.adapters.incoming.stream

import com.alo.loan.application.services.EvaluateLoan
import com.alo.loan.application.services.LoanEvaluationRequest
import com.alo.loan.infrastructure.fake.Event
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.lang.Exception
import java.math.BigDecimal
import java.util.UUID

class InMemoryLoanEvaluationStreamConsumer(
    private val evaluateLoan: EvaluateLoan,
    private val objectMapper: ObjectMapper
) {
    // omitting all error handling since this is a fake inmemory impl for the sake of the demo
    fun reactTo(event: Event) =
        if(event.eventType == "LoanEvaluationRequestEvent")
            objectMapper
                .readValue<LoanEvaluationRequestEvent>(event.eventPayload)
                .let { LoanEvaluationRequest(it.id, it.customerId, it.amount)  }
                .also { evaluateLoan(it) }
        else throw Exception("Not recognized event '${event.eventType}'")
}

data class LoanEvaluationRequestEvent(
    val id: UUID, val customerId: UUID, val amount: BigDecimal
)
