package com.example.travel

import android.util.Log
import retrofit2.HttpException

/**
 * 地址转经纬度：GET /api/amap/geocode（与 Web AmapController 一致）
 */
object GeocodeHelper {

    private const val TAG = "GeocodeHelper"

    suspend fun fetchCoordinates(address: String): Pair<Double, Double>? {
        val trimmed = address.trim()
        if (trimmed.length <= 2) return null

        return try {
            val res = NetworkClient.apiService.amapGeocode(trimmed)
            if (res.success && res.latitude != null && res.longitude != null) {
                val lat = res.latitude!!
                val lng = res.longitude!!
                if (lat != 0.0 || lng != 0.0) {
                    Log.d(TAG, "geocode ok: $lat, $lng")
                    lat to lng
                } else {
                    Log.w(TAG, "geocode returned 0,0")
                    null
                }
            } else {
                Log.w(TAG, "geocode failed: ${res.message}")
                tryApiResponseFormat(trimmed)
            }
        } catch (e: HttpException) {
            Log.e(TAG, "geocode HTTP ${e.code()}", e)
            tryApiResponseFormat(trimmed)
        } catch (e: Exception) {
            Log.e(TAG, "geocode error: ${e.message}", e)
            null
        }
    }

    private suspend fun tryApiResponseFormat(address: String): Pair<Double, Double>? {
        return try {
            val res = NetworkClient.apiService.amapGeocodeApi(address)
            if (res.success && res.data != null) {
                val lat = res.data.latitude
                val lng = res.data.longitude
                if (lat != 0.0 || lng != 0.0) lat to lng else null
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
