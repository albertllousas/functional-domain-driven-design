package com.alo.loan.infrastructure.adapters.outgoing.events

import com.alo.loan.domain.model.DomainEvent
import com.alo.loan.domain.model.PublishEvents

interface DomainEventSubscriber : (DomainEvent) -> Unit

// in a real environment we would face here a dual write problem, having to apply transactional outbox pattern or other solution
class InMemoryDomainEventPublisher(private val subscribers: List<DomainEventSubscriber>) : PublishEvents {
    override fun invoke(domainEvents: List<DomainEvent>) {
        subscribers.forEach { subscriber -> domainEvents.forEach(subscriber) }
    }
}
