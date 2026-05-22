package com.example.travel

/**
 * 高德配置（与 Web landscape-detail.html、AmapService 保持一致）
 */
object MapConstants {
    const val WEB_JS_KEY = "ee7b7be648faf8d9200c001ecab389b0"
    const val JS_VERSION = "1.4.15"
    const val REST_KEY = "e5ca958a2702e97a1fe428b536c98fdb"

    fun staticMapUrl(latitude: Double, longitude: Double): String =
        "https://restapi.amap.com/v3/staticmap?location=$longitude,$latitude&zoom=15&size=750*300&scale=2" +
            "&markers=mid,,A:$longitude,$latitude&key=$REST_KEY"
}
