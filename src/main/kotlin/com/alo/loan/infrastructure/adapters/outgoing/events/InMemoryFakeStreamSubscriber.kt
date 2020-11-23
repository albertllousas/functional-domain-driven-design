package com.alo.loan.infrastructure.adapters.outgoing.events

import com.alo.loan.domain.model.DomainEvent
import com.alo.loan.infrastructure.fake.Event
import com.alo.loan.infrastructure.fake.InMemoryFakeEventStream
import com.fasterxml.jackson.databind.ObjectMapper

class InMemoryFakeStreamSubscriber(
    private val inMemoryFakeEventStream: InMemoryFakeEventStream,
    private val objectMapper: ObjectMapper,
    private val stream: String
) : DomainEventSubscriber {
    override fun invoke(domainEvent: DomainEvent) {
        inMemoryFakeEventStream.publish(
            stream = stream,
            event = Event(domainEvent::class.simpleName!!, objectMapper.writeValueAsBytes(domainEvent))
        )
    }
}
