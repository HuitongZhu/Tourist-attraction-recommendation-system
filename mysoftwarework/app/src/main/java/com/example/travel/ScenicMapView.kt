package com.example.travel

import android.util.Log
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size

private const val TAG = "ScenicMapView"

/**
 * 景点地图：使用静态地图API显示
 */
@Composable
fun ScenicMapView(
    latitude: Double?,
    longitude: Double?,
    title: String?,
    address: String?,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://restapi.amap.com/v3/staticmap?location=118.797628,32.044032&zoom=15&size=750*300&scale=2&markers=mid,,A:118.797628,32.044032&key=e5ca958a2702e97a1fe428b536c98fdb")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val code = connection.responseCode
                Log.d(TAG, "HTTP $code for debug test")
                if (code == 200) {
                    Log.d(TAG, "Success - image returned")
                } else {
                    val body = connection.errorStream?.bufferedReader()?.readText()?.take(300) ?: "no body"
                    Log.d(TAG, "Error body: $body")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Request exception: $e")
            }
        }
    }
    if (latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0)) {
        MapPlaceholder(address = address, latitude = latitude, longitude = longitude, modifier = modifier)
        return
    }

    var retryCount by remember { mutableStateOf(0) }
    val maxRetries = 2
    val mapUrl = MapConstants.staticMapUrl(latitude, longitude)
    
    Log.d(TAG, "Loading map for $title: lat=$latitude, lng=$longitude, url=$mapUrl")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFE8E8E8))
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mapUrl.replace("%2A", "*"))
                .size(Size.ORIGINAL)
                .build(),
            contentDescription = title ?: "景点位置",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .then(Modifier),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            },
            error = {
                if (retryCount < maxRetries) {
                    Log.w(TAG, "Map load failed, retrying (attempt ${retryCount + 1})...")
                    retryCount++
                } else {
                    Log.e(TAG, "Map load failed after $maxRetries attempts for $title")
                    MapPlaceholder(
                        address = address,
                        latitude = latitude,
                        longitude = longitude,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
private fun MapPlaceholder(
    address: String?,
    latitude: Double?,
    longitude: Double?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFE8E8E8)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = Color(0xFF1A56DB),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = address ?: "暂无地址", color = Color.DarkGray, fontSize = 14.sp)
            if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
                Text(text = "坐标: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}", fontSize = 12.sp, color = Color.Gray)
            } else {
                Text(text = "暂无有效坐标，请重新编辑景点地址并保存", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
