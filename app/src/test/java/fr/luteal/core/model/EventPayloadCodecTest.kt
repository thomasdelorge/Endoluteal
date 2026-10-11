package fr.luteal.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EventPayloadCodecTest {

    @Test
    fun eachPayloadSubtypeRoundTrips() {
        JournalEventType.entries.forEach { type ->
            val payload = EventPayload.of(type)
            val decoded = EventPayloadCodec.decode(EventPayloadCodec.encode(payload))
            assertEquals(payload, decoded)
        }
    }
}
