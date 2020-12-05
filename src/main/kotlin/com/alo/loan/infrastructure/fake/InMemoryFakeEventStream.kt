package com.alo.loan.infrastructure.fake

typealias EventHandler = (Event) -> Unit

// Just in memory stream impl, in a prod code it could be kafka, kinesis, rabbitmq.
// Don't pay attention on the impl, just for testing purposes
class InMemoryFakeEventStream {

    private val streams: MutableMap<String, List<Event>> = mutableMapOf()

    private val streamHandlers: MutableMap<String, List<EventHandler>> = mutableMapOf()

    fun subscribe(stream: String, handler: EventHandler) {
        if (streamHandlers.containsKey(stream))
            streamHandlers.computeIfPresent(stream) { _, handlers -> handlers + handler }
        else streamHandlers.put(stream, listOf(handler))
    }

    fun publish(event: Event, stream: String) {
        if (streams.containsKey(stream))
            streams.computeIfPresent(stream) { _, events -> events + event }
        else streams.put(stream, listOf(event))
        streamHandlers[stream]?.forEach { it.invoke(event) }
    }
}

data class Event(val eventType: String, val eventPayload: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (eventType != other.eventType) return false
        if (!eventPayload.contentEquals(other.eventPayload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventType.hashCode()
        result = 31 * result + eventPayload.contentHashCode()
        return result
    }
}
