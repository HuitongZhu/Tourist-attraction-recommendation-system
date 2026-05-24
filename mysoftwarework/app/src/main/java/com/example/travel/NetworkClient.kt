package com.example.travel

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    // 1. 更新为后端最新的 IP 地址
    // 注意：Android模拟器访问本地服务器需要使用 10.0.2.2
    // 如果是真实设备测试，使用开发机的局域网IP（如 192.168.x.x）
    const val BASE_URL = "http://10.100.27.123:8080/"

    fun mediaUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val normalized = if (path.startsWith("/")) path.drop(1) else path
        return BASE_URL + normalized
    }

    // 用于保存登录后的用户信息（实际开发中建议存入 SharedPreferences）
    var userToken: String? = null
    var userId: String? = null
    var userName: String? = null
    var userType: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. 添加 Auth 拦截器
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        // 如果 userId 不为空且不是登录/注册接口，则添加 Header
        val path = original.url.encodedPath
        val userId = NetworkClient.userId
        if (!userId.isNullOrEmpty()) {
            // 登录和注册接口不需要 userId
            if (!path.contains("/api/login") &&
                !path.contains("/api/register") &&
                !path.contains("/api/send-sms-code") &&
                !path.contains("/api/sms-send-code") &&
                !path.contains("/api/register/sms-code")) {
                requestBuilder.header("X-User-Id", userId)
            }
        }

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    private val gson = GsonBuilder()
        .registerTypeAdapter(Double::class.javaObjectType, FlexibleDoubleTypeAdapter())
        .registerTypeAdapter(Double::class.javaPrimitiveType, FlexibleDoubleTypeAdapter())
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}