package com.example.travel

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.HttpException
import java.net.URLEncoder

/**
 * 地址转经纬度：优先后端 /api/amap，失败时 App 直连高德 REST
 */
object GeocodeHelper {

    private const val TAG = "GeocodeHelper"
    private const val AMAP_GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo"

    private val directClient = OkHttpClient.Builder().build()

    suspend fun fetchCoordinates(address: String, alternateQuery: String? = null): Pair<Double, Double>? {
        val queries = buildQueryList(address, alternateQuery)
        if (queries.isEmpty()) return null

        for (query in queries) {
            val title = if (query == address.trim()) alternateQuery else null
            fetchFromBackend(query, title)?.let { return it }
        }
        for (query in queries) {
            fetchFromAmapDirect(query)?.let { return it }
        }

        Log.w(TAG, "all geocode paths failed for: $queries")
        return null
    }

    private fun buildQueryList(address: String, alternateQuery: String?): List<String> {
        val result = linkedSetOf<String>()
        val trimmed = address.trim()
        if (trimmed.length > 2) {
            result.add(trimmed)
            stripParentheses(trimmed).takeIf { it.length > 2 && it != trimmed }?.let { result.add(it) }
        }
        alternateQuery?.trim()?.takeIf { it.length > 2 }?.let { alt ->
            if (!result.contains(alt)) result.add(alt)
        }
        return result.toList()
    }

    private fun stripParentheses(text: String): String {
        return text
            .replace(Regex("\\([^)]*\\)"), "")
            .replace(Regex("（[^）]*）"), "")
            .replace(Regex("\\s+"), "")
            .trim()
    }

    private suspend fun fetchFromBackend(address: String, title: String? = null): Pair<Double, Double>? {
        return try {
            val res = NetworkClient.apiService.amapGeocode(address, title)
            parseBackendFlat(res.success, res.latitude, res.longitude, res.message)
        } catch (e: HttpException) {
            Log.w(TAG, "backend geocode HTTP ${e.code()}, try geocode-api")
            tryBackendApiFormat(address, title)
        } catch (e: Exception) {
            Log.w(TAG, "backend geocode error: ${e.message}, try geocode-api")
            tryBackendApiFormat(address, title)
        }
    }

    private suspend fun tryBackendApiFormat(address: String, title: String? = null): Pair<Double, Double>? {
        return try {
            val res = NetworkClient.apiService.amapGeocodeApi(address, title)
            if (res.success && res.data != null) {
                parseBackendFlat(true, res.data.latitude, res.data.longitude, res.message)
            } else {
                Log.w(TAG, "geocode-api failed: ${res.message}")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "geocode-api error: ${e.message}")
            null
        }
    }

    private fun parseBackendFlat(
        success: Boolean,
        latitude: Double?,
        longitude: Double?,
        message: String?
    ): Pair<Double, Double>? {
        if (!success || latitude == null || longitude == null) {
            if (message != null) Log.w(TAG, "geocode failed: $message")
            return null
        }
        if (latitude == 0.0 && longitude == 0.0) {
            Log.w(TAG, "geocode returned 0,0")
            return null
        }
        Log.d(TAG, "geocode ok (backend): $latitude, $longitude")
        return latitude to longitude
    }

    private suspend fun fetchFromAmapDirect(address: String): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(address, Charsets.UTF_8.name())
                val url = "$AMAP_GEOCODE_URL?address=$encoded&key=${MapConstants.REST_KEY}&output=JSON"
                val response = directClient.newCall(Request.Builder().url(url).get().build()).execute()
                response.use {
                    val body = it.body?.string()
                    if (!it.isSuccessful || body.isNullOrBlank()) {
                        Log.w(TAG, "amap direct HTTP ${it.code}")
                        return@withContext null
                    }
                    val json = JSONObject(body)
                    if (json.optString("status") != "1") {
                        Log.w(TAG, "amap direct: ${json.optString("info")} (${json.optString("infocode")})")
                        return@withContext null
                    }
                    val geocodes = json.optJSONArray("geocodes") ?: return@withContext null
                    if (geocodes.length() == 0) return@withContext null
                    val location = geocodes.getJSONObject(0).optString("location")
                    val parts = location.split(",")
                    if (parts.size != 2) return@withContext null
                    val lng = parts[0].trim().toDoubleOrNull() ?: return@withContext null
                    val lat = parts[1].trim().toDoubleOrNull() ?: return@withContext null
                    if (lat == 0.0 && lng == 0.0) return@withContext null
                    Log.d(TAG, "geocode ok (amap direct): $lat, $lng")
                    lat to lng
                }
            } catch (e: Exception) {
                Log.e(TAG, "amap direct error: ${e.message}", e)
                null
            }
        }
}
