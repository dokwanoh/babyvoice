package com.babyvoice.bridge.data.partnerapi

import com.babyvoice.bridge.core.model.BabyId
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class HttpPartnerApiClient(
    private val baseUrl: URL,
    private val authStore: PartnerApiAuthStore,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    },
) : PartnerApiClient {
    override suspend fun fetchBabies(): Result<List<PartnerBabyDto>> = runCatching {
        val body = request("/v1/me/babies")
        json.decodeFromString<PartnerBabyListEnvelope>(body).babies
    }

    override suspend fun fetchSnapshot(babyId: BabyId): Result<PartnerSnapshotDto> =
        Result.failure(IllegalStateException("snapshot_mapping_not_implemented"))

    override suspend fun fetchActivities(babyId: BabyId, cursor: String?): Result<PartnerActivitiesDto> = runCatching {
        val suffix = cursor?.let { "?cursor=$it" }.orEmpty()
        val body = request("/v1/babies/${babyId.value}/activities$suffix")
        json.decodeFromString<PartnerActivitiesDto>(body)
    }

    private fun request(path: String): String {
        val url = URL(baseUrl, path)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 3_000
            readTimeout = 3_000
            authStore.accessToken?.let { setRequestProperty("Authorization", "Bearer $it") }
        }
        return try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                throw IllegalStateException("http_$status")
            }
            body
        } finally {
            connection.disconnect()
        }
    }
}

@Serializable
private data class PartnerBabyListEnvelope(
    val babies: List<PartnerBabyDto>,
)
