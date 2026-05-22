package com.example.travel

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

/**
 * 景点地图：底层静态图（本地拼接 URL，不依赖 404 接口）+ 上层 Web 高德 JS
 */
@Composable
fun ScenicMapView(
    latitude: Double?,
    longitude: Double?,
    title: String?,
    address: String?,
    modifier: Modifier = Modifier
) {
    if (latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0)) {
        MapPlaceholder(address = address, latitude = latitude, longitude = longitude, modifier = modifier)
        return
    }

    val lat = latitude
    val lng = longitude
    val staticUrl = remember(lat, lng) { MapConstants.staticMapUrl(lat, lng) }
    val html = remember(lat, lng, title, address) {
        buildAmapHtml(
            webJsKey = MapConstants.WEB_JS_KEY,
            jsVersion = MapConstants.JS_VERSION,
            longitude = lng,
            latitude = lat,
            title = title ?: "",
            address = address ?: ""
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFE8E8E8))
    ) {
        AsyncImage(
            model = staticUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        ScenicMapWebView(html = html, modifier = Modifier.fillMaxSize())
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ScenicMapWebView(
    html: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                webViewClient = WebViewClient()
                setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                loadDataWithBaseURL(
                    "https://webapi.amap.com/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = modifier
    )
}

private fun buildAmapHtml(
    webJsKey: String,
    jsVersion: String,
    longitude: Double,
    latitude: Double,
    title: String,
    address: String
): String {
    val safeTitle = escapeJs(title)
    val safeAddress = escapeJs(address)
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"/>
          <script src="https://webapi.amap.com/maps?v=$jsVersion&key=$webJsKey"></script>
          <style>
            html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: transparent !important; }
            #map { width: 100%; height: 100%; }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script>
            var map = new AMap.Map('map', {
              resizeEnable: true,
              zoom: 15,
              center: [$longitude, $latitude]
            });
            var marker = new AMap.Marker({
              position: [$longitude, $latitude],
              title: '$safeTitle'
            });
            map.add(marker);
          </script>
        </body>
        </html>
    """.trimIndent()
}

private fun escapeJs(text: String): String =
    text.replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\r", " ")
        .replace("\n", " ")

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
                Text(text = "坐标: $latitude, $longitude", fontSize = 12.sp, color = Color.Gray)
            } else {
                Text(text = "暂无有效坐标，请重新编辑景点地址并保存", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
