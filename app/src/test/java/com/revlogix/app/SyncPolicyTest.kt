package com.revlogix.app

import com.revlogix.app.data.sync.SyncPolicy
import com.revlogix.app.data.sync.SyncPolicy.Outcome
import com.revlogix.app.data.sync.SyncTypes
import com.revlogix.app.data.local.SyncQueueItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncPolicyTest {

    @Test
    fun `success codes finish the queue item`() {
        assertEquals(Outcome.DONE, SyncPolicy.outcomeForHttp(200))
        assertEquals(Outcome.DONE, SyncPolicy.outcomeForHttp(204))
    }

    @Test
    fun `server errors and expired tokens are retried later`() {
        assertEquals(Outcome.RETRY, SyncPolicy.outcomeForHttp(500))
        assertEquals(Outcome.RETRY, SyncPolicy.outcomeForHttp(503))
        assertEquals(Outcome.RETRY, SyncPolicy.outcomeForHttp(401))
        assertEquals(Outcome.RETRY, SyncPolicy.outcomeForHttp(429))
    }

    @Test
    fun `client errors are not retried`() {
        assertEquals(Outcome.GIVE_UP, SyncPolicy.outcomeForHttp(400))
        assertEquals(Outcome.GIVE_UP, SyncPolicy.outcomeForHttp(404))
    }

    @Test
    fun `item is marked failed after the maximum number of attempts`() {
        assertFalse(SyncPolicy.hasExhaustedRetries(0))
        assertFalse(SyncPolicy.hasExhaustedRetries(SyncPolicy.MAX_RETRIES - 2))
        assertTrue(SyncPolicy.hasExhaustedRetries(SyncPolicy.MAX_RETRIES - 1))
    }

    @Test
    fun `new queue items start as pending with no retries`() {
        val item = SyncQueueItem(
            entityType = SyncTypes.VEHICLES,
            entityId = 7,
            operation = SyncTypes.CREATE,
            payloadJson = "{}"
        )
        assertEquals("PENDING", item.status)
        assertEquals(0, item.retryCount)
    }
}
