package com.osucad.server.messageOrderer

import com.osucad.server.multiplayer.DocumentMessage
import com.osucad.server.multiplayer.RawOperationMessage
import com.osucad.server.multiplayer.SequencedOperationMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MessageOrdererTest {

    @Test
    fun `MessageOrderer assigns increasing sequence numbers`(): Unit = ordererTest {
        orderer.handle(
            RawOperationMessage(
                operation = DocumentMessage.ClientJoin(
                    clientId = "client1",
                    clientSequenceNumber = 0,
                    referenceSequenceNumber = 0,
                ),
                clientId = "client1",
                timestamp = Clock.System.now(),
            )
        )

        assertEquals(1, deltas.receive().operation.sequenceNumber)

        orderer.handle(
            RawOperationMessage(
                operation = DocumentMessage.ClientJoin(
                    clientId = "client2",
                    clientSequenceNumber = 0,
                    referenceSequenceNumber = 0,
                ),
                clientId = "client2",
                timestamp = Clock.System.now(),
            )
        )

        assertEquals(2, deltas.receive().operation.sequenceNumber)
    }

    @Test
    fun `message orderer should ignore repeated join mesages from the same client`() = ordererTest {
        orderer.handle(
            RawOperationMessage(
                operation = DocumentMessage.ClientJoin(
                    clientId = "client1",
                    clientSequenceNumber = 0,
                    referenceSequenceNumber = 0,
                ),
                clientId = "client1",
                timestamp = Clock.System.now(),
            )
        )

        assertEquals(1, deltas.receive().operation.sequenceNumber)

        orderer.handle(
            RawOperationMessage(
                operation = DocumentMessage.ClientJoin(
                    clientId = "client1",
                    clientSequenceNumber = 0,
                    referenceSequenceNumber = 0,
                ),
                clientId = "client1",
                timestamp = Clock.System.now(),
            )
        )

        assertNull(deltas.tryReceive().getOrNull())
    }

    @Test
    fun `message orderer should ignore repeated leave mesages from the same client`() = ordererTest {
        orderer.handle(
            RawOperationMessage(
                operation = DocumentMessage.ClientJoin(
                    clientId = "client1",
                    clientSequenceNumber = 0,
                    referenceSequenceNumber = 0,
                ),
                clientId = "client1",
                timestamp = Clock.System.now(),
            )
        )

        assertEquals(1, deltas.receive().operation.sequenceNumber)

        orderer.handle(
            RawOperationMessage(
                operation = DocumentMessage.ClientLeave(
                    clientId = "client1",
                    clientSequenceNumber = 0,
                    referenceSequenceNumber = 0,
                ),
                clientId = "client1",
                timestamp = Clock.System.now(),
            )
        )

        assertEquals(2, deltas.receive().operation.sequenceNumber)

        orderer.handle(
            RawOperationMessage(
                operation = DocumentMessage.ClientLeave(
                    clientId = "client1",
                    clientSequenceNumber = 0,
                    referenceSequenceNumber = 0,
                ),
                clientId = "client1",
                timestamp = Clock.System.now(),
            )
        )

        assertNull(deltas.tryReceive().getOrNull())
    }

    private fun ordererTest(block: suspend OrdererTestContext.() -> Unit) {
        runBlocking {
            val deltas = Channel<SequencedOperationMessage>(capacity = Channel.UNLIMITED)

            val orderer = MessageOrderer(
                documentId = "foo",
                deltasProducer = deltas,
                clientSeqManager = ClientSequenceNumberManager()
            )

            OrdererTestContext(orderer, deltas).block()
        }
    }

    private class OrdererTestContext(
        val orderer: MessageOrderer,
        val deltas: Channel<SequencedOperationMessage>,
    )
}