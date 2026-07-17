package com.babyvoice.bridge.data.partnerapi

import com.babyvoice.bridge.core.model.BabyId
import java.net.URL
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class HttpPartnerApiClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: HttpPartnerApiClient

    @Before
    fun setUp() {
        server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setBody("""{"babies":[{"id":"haram","name":"하람","isPrimary":true,"isTwin":false}]}"""),
            )
            enqueue(
                MockResponse()
                    .setBody("""{"activities":[],"nextCursor":null}"""),
            )
            start()
        }
        client = HttpPartnerApiClient(
            baseUrl = server.url("/").toUrl(),
            authStore = object : PartnerApiAuthStore {
                override val accessToken: String? = null
                override suspend fun saveAccessToken(token: String) = Unit
                override suspend fun clear() = Unit
            },
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun fetchesBabiesAndSnapshotFromFakeServer() = runBlocking {
        val babies = client.fetchBabies().getOrThrow()
        val activities = client.fetchActivities(BabyId("haram"), null).getOrThrow()

        assertEquals(1, babies.size)
        assertEquals("하람", babies.first().name)
        assertEquals(0, activities.activities.size)
        assertEquals("/v1/me/babies", server.takeRequest().path)
        assertEquals("/v1/babies/haram/activities", server.takeRequest().path)
    }
}
