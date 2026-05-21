package com.example.travel

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // --- 认证模块 ---
    @FormUrlEncoded
    @POST("/api/login")
    suspend fun login(
        @Field("account") account: String,
        @Field("password") password: String?,
        @Field("code") code: String?,
        @Field("userType") userType: String = "2",
        @Field("loginType") loginType: String = "password"
    ): retrofit2.Response<okhttp3.ResponseBody>
    
    @FormUrlEncoded
    @POST("/api/register")
    suspend fun register(
        @Field("userName") userName: String,
        @Field("account") account: String,
        @Field("password") password: String,
        @Field("confirm_password") confirmPassword: String
    ): retrofit2.Response<okhttp3.ResponseBody>
    
    // 发送短信验证码（登录用）
    @FormUrlEncoded
    @POST("/api/send-sms-code")
    suspend fun sendSms(@Field("phone") phone: String): retrofit2.Response<okhttp3.ResponseBody>
    
    // 发送注册验证码
    @FormUrlEncoded
    @POST("/api/register/send-code")
    suspend fun sendRegisterCode(@Field("phone") phone: String): retrofit2.Response<okhttp3.ResponseBody>
    
    // 验证注册验证码
    @FormUrlEncoded
    @POST("/api/register/verify-code")
    suspend fun verifyRegisterCode(@Field("phone") phone: String, @Field("code") code: String): retrofit2.Response<okhttp3.ResponseBody>

    // --- 景点模块 ---
    @GET("/api/landscapes/home")
    suspend fun getLandscapes(
        @Query("keyword") keyword: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<LandscapeResponse>>

    @GET("/api/landscapes/{id}")
    suspend fun getLandscapeById(@Path("id") id: String): Response<ApiResponse<LandscapeBackendResponse>>

    // --- 帖子模块 ---
    @GET("/api/posts")
    suspend fun getPosts(
        @Query("keyword") keyword: String? = null,
        @Query("tag") tag: String? = null,
        @Query("status") status: String? = null,
        @Query("landscapeId") landscapeId: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PostListResponse>

    @GET("/api/posts/{id}")
    suspend fun getPostById(@Path("id") id: String): ApiResponse<PostBackendResponse>

    // --- 评论模块 ---
    @GET("/api/comments")
    suspend fun getComments(
        @Query("landscapeId") landscapeId: String? = null,
        @Query("postId") postId: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<CommentResponse>>

    @DELETE("/api/comments/{id}")
    suspend fun deleteComment(@Path("id") id: String): ApiResponse<Unit>

    // --- 点赞收藏模块 ---
    @POST("/api/interactions/favorites")
    suspend fun addFavorite(@Body request: FavoriteRequest): ApiResponse<InteractionResponse>

    @DELETE("/api/interactions/favorites/{id}")
    suspend fun deleteFavorite(@Path("id") id: String): ApiResponse<Unit>

    @POST("/api/interactions/likes")
    suspend fun addLike(@Body request: LikeRequest): ApiResponse<InteractionResponse>

    @DELETE("/api/interactions/likes/{id}")
    suspend fun deleteLike(@Path("id") id: String): ApiResponse<Unit>

    // --- 用户模块 ---
    @GET("/api/users/me")
    suspend fun getCurrentUser(): ApiResponse<UserResponse>

    @PUT("/api/users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserResponse>

    // --- 管理员用户管理 ---
    @GET("/api/admin/users")
    suspend fun getAllUsers(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): ApiResponse<PageResponse<UserResponse>>

    @GET("/api/admin/users/{id}")
    suspend fun getUserById(@Path("id") id: String): ApiResponse<UserResponse>

    @DELETE("/api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): ApiResponse<Unit>

    // --- 地图模块 ---
    @GET("/api/maps/geocode")
    suspend fun geocode(@Query("address") address: String): ApiResponse<GeocodeResponse>

    @POST("/api/maps/geocode")
    suspend fun geocodePost(@Body request: GeocodeRequest): ApiResponse<GeocodeResponse>

    // --- 管理员审核模块 ---
    @GET("/api/admin/landscapes")
    suspend fun getAllLandscapes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<LandscapeResponse>>

    @PATCH("/api/admin/landscapes/{id}/audit")
    suspend fun auditLandscape(@Path("id") id: String, @Body request: AuditRequest): ApiResponse<LandscapeResponse>
}
