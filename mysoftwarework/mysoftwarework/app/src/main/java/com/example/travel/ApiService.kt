package com.example.travel

import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    /** 统一发送验证码（返回 smsCode 供弹窗展示）type: login|register|password|delete|forgot */
    @FormUrlEncoded
    @POST("/api/sms-send-code")
    suspend fun sendSmsCode(
        @Field("phone") phone: String,
        @Field("type") type: String
    ): ApiResponse<SmsSendResponse>

    @FormUrlEncoded
    @POST("/api/register/sms-code")
    suspend fun sendSmsCodeRegister(
        @Field("phone") phone: String,
        @Field("type") type: String
    ): ApiResponse<SmsSendResponse>

    // 兼容旧路径（仍可用，但不返回验证码）
    @FormUrlEncoded
    @POST("/api/send-sms-code")
    suspend fun sendSms(@Field("phone") phone: String): retrofit2.Response<okhttp3.ResponseBody>

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
    ): ApiResponse<List<LandscapeResponse>>

    /** 首页全部已审核景点 + 模糊搜索（优先于 /api/landscapes/home 的 8 条限制） */
    @GET("/api/landscapes/home-list")
    suspend fun getHomeLandscapeList(
        @Query("keyword") keyword: String? = null
    ): ApiResponse<List<LandscapeBackendResponse>>

    @GET("/api/landscapes/{id}")
    suspend fun getLandscapeById(@Path("id") id: String): Response<ApiResponse<LandscapeBackendResponse>>

    /** 景点详情（dto 显式返回经纬度，优先于 /api/landscapes/{id}） */
    @GET("/api/landscape-detail/{id}")
    suspend fun getLandscapeDetail(@Path("id") id: String): ApiResponse<LandscapeBackendResponse>

    /** 已审核通过的景点（发布推荐帖关联用） */
    @GET("/api/landscapes/approved")
    suspend fun getApprovedLandscapes(): ApiResponse<List<LandscapeBackendResponse>>

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

    @PUT("/api/my/comments/{id}")
    suspend fun updateMyComment(
        @Path("id") id: String,
        @Body request: CommentUpdateRequest
    ): ApiResponse<CommentResponse>

    @PUT("/api/user-comment/{id}")
    suspend fun updateUserComment(
        @Path("id") id: String,
        @Body request: CommentUpdateRequest
    ): ApiResponse<CommentResponse>

    // --- 点赞收藏模块 ---
    @GET("/api/interactions/status")
    suspend fun getInteractionStatus(
        @Query("landscapeId") landscapeId: String? = null,
        @Query("postId") postId: String? = null
    ): ApiResponse<InteractionStatusResponse>

    @GET("/api/users/me/interactions/landscapes/likes")
    suspend fun getMyLandscapeLikes(): ApiResponse<List<LandscapeBackendResponse>>

    @GET("/api/users/me/interactions/landscapes/favorites")
    suspend fun getMyLandscapeFavorites(): ApiResponse<List<LandscapeBackendResponse>>

    @GET("/api/users/me/interactions/posts/likes")
    suspend fun getMyPostLikes(): ApiResponse<List<PostBackendResponse>>

    @GET("/api/users/me/interactions/posts/favorites")
    suspend fun getMyPostFavorites(): ApiResponse<List<PostBackendResponse>>

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

    @GET("/api/user-profile")
    suspend fun getUserProfile(): ApiResponse<UserResponse>

    @PUT("/api/user-profile")
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

    @GET("/api/admin/users/{id}/detail")
    suspend fun getAdminUserDetail(@Path("id") id: String): ApiResponse<UserResponse>

    @PUT("/api/admin/users/{id}/profile")
    suspend fun updateAdminUserProfile(
        @Path("id") id: String,
        @Body request: AdminUpdateUserRequest
    ): ApiResponse<UserResponse>

    @DELETE("/api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): ApiResponse<Unit>

    // --- 地图模块（与 Web 端 AmapController 一致）---
    @GET("/api/amap/geocode")
    suspend fun amapGeocode(@Query("address") address: String): AmapGeocodeResponse

    @GET("/api/amap/geocode-api")
    suspend fun amapGeocodeApi(@Query("address") address: String): ApiResponse<GeocodeResponse>

    @GET("/api/amap/map-config")
    suspend fun getAmapMapConfig(): ApiResponse<AmapMapConfigResponse>

    @GET("/api/amap/static-map-url")
    suspend fun getAmapStaticMapUrl(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): ApiResponse<String>

    /** 发布景点（服务端自动 geocode 补全经纬度） */
    @Multipart
    @POST("/api/app/landscapes/publish")
    suspend fun publishLandscapeApp(
        @Part("title") title: RequestBody,
        @Part("address") address: RequestBody,
        @Part("content") content: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("tel") tel: RequestBody?,
        @Part("openingTime") openingTime: RequestBody?,
        @Part("level") level: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ApiResponse<LandscapeBackendResponse>

    // --- 管理员审核模块 ---
    @GET("/api/admin/landscapes")
    suspend fun getAllLandscapes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<LandscapeResponse>>

    @GET("/api/admin/review/landscapes")
    suspend fun getAdminReviewLandscapes(
        @Query("filter") filter: String = AdminReviewFilter.ALL,
        @Query("keyword") keyword: String? = null
    ): ApiResponse<List<LandscapeResponse>>

    @PATCH("/api/admin/review/landscapes/{id}/audit")
    suspend fun auditLandscape(@Path("id") id: String, @Body request: AuditRequest): ApiResponse<LandscapeResponse>

    /** 管理员查看景点详情（含待审核、未通过） */
    @GET("/api/admin/review/landscapes/{id}")
    suspend fun getAdminReviewLandscapeDetail(@Path("id") id: String): ApiResponse<LandscapeResponse>

    /** 管理员景点详情（备用路径，与 PostPublishController 一致） */
    @GET("/api/admin-landscape-detail/{id}")
    suspend fun getAdminLandscapeDetail(@Path("id") id: String): ApiResponse<LandscapeResponse>

    /** 管理员推荐帖详情（任意审核状态） */
    @GET("/api/admin-post-detail/{id}")
    suspend fun getAdminPostDetail(@Path("id") id: String): ApiResponse<PostBackendResponse>

    @DELETE("/api/admin/review/landscapes/{id}")
    suspend fun deleteAdminReviewLandscape(@Path("id") id: String): ApiResponse<Unit>

    @GET("/api/admin/review/posts")
    suspend fun getAdminReviewPosts(
        @Query("filter") filter: String = AdminReviewFilter.ALL,
        @Query("keyword") keyword: String? = null
    ): ApiResponse<List<PostBackendResponse>>

    @PATCH("/api/admin/review/posts/{id}/audit")
    suspend fun auditPost(@Path("id") id: String, @Body request: AuditRequest): ApiResponse<PostBackendResponse>

    @DELETE("/api/admin/review/posts/{id}")
    suspend fun deleteAdminReviewPost(@Path("id") id: String): ApiResponse<Unit>

    @GET("/api/admin/review/comments")
    suspend fun getAdminReviewComments(
        @Query("keyword") keyword: String? = null
    ): ApiResponse<List<CommentReviewResponse>>

    @DELETE("/api/admin/review/comments/{id}")
    suspend fun deleteAdminComment(@Path("id") id: String): ApiResponse<Unit>

    // --- 用户模块补充 ---
    // 修改密码（原密码验证）
    @FormUrlEncoded
    @POST("/api/user-change-password")
    suspend fun changePassword(
        @Field("oldPassword") oldPassword: String,
        @Field("newPassword") newPassword: String
    ): ApiResponse<Unit>

    // 通过短信验证码重置密码
    @FormUrlEncoded
    @POST("/api/user-reset-password")
    suspend fun resetPasswordBySms(
        @Field("phone") phone: String,
        @Field("code") code: String,
        @Field("newPassword") newPassword: String
    ): ApiResponse<Unit>

    // 检查用户名是否存在
    @GET("/api/register/check-username")
    suspend fun checkUsername(@Query("username") username: String): ApiResponse<UsernameCheckResponse>

    // 检查手机号是否已注册
    @GET("/api/register/check-account")
    suspend fun checkAccount(@Query("account") account: String): ApiResponse<UsernameCheckResponse>

    // --- 发布模块 ---
    // 创建景点（JSON，无图）
    @POST("/api/landscapes/publish")
    suspend fun createLandscape(@Body request: LandscapeRequest): ApiResponse<LandscapeBackendResponse>

    // 创建景点（multipart，支持图片上传）
    @Multipart
    @POST("/api/landscapes/publish")
    suspend fun publishLandscapeMultipart(
        @Part("title") title: RequestBody,
        @Part("address") address: RequestBody,
        @Part("content") content: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("tel") tel: RequestBody?,
        @Part("openingTime") openingTime: RequestBody?,
        @Part("level") level: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ApiResponse<LandscapeBackendResponse>

    // 创建帖子
    @POST("/api/posts/publish")
    suspend fun publishPost(@Body request: PostRequest): ApiResponse<PostBackendResponse>

    // --- 个人中心：我的内容 ---
    @GET("/api/my/landscapes")
    suspend fun getMyLandscapes(): ApiResponse<List<LandscapeBackendResponse>>

    @GET("/api/my/landscapes/{id}")
    suspend fun getMyLandscapeById(@Path("id") id: String): ApiResponse<LandscapeBackendResponse>

    @PUT("/api/my/landscapes/{id}")
    suspend fun updateMyLandscape(
        @Path("id") id: String,
        @Body request: LandscapeRequest
    ): ApiResponse<LandscapeBackendResponse>

    @DELETE("/api/my/landscapes/{id}")
    suspend fun deleteMyLandscape(@Path("id") id: String): ApiResponse<Unit>

    @GET("/api/my/posts")
    suspend fun getMyPosts(): ApiResponse<List<PostBackendResponse>>

    @DELETE("/api/my/posts/{id}")
    suspend fun deleteMyPost(@Path("id") id: String): ApiResponse<Unit>

    // --- 评论模块补充 ---
    // 创建评论
    @POST("/api/comments")
    suspend fun createComment(@Body request: CommentRequest): ApiResponse<CommentResponse>

    // --- 首页模块 ---
    // 获取首页内容（HTML格式）
    @GET("/")
    suspend fun getHomePage(): retrofit2.Response<okhttp3.ResponseBody>

    // --- 管理员用户管理补充 ---
    // 更新用户状态
    @PATCH("/api/admin/users/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: String, @Body request: UserStatusRequest): ApiResponse<UserResponse>

    // --- 用户注销模块 ---
    // 验证密码（用于注销）
    @FormUrlEncoded
    @POST("/api/users/verify-password")
    suspend fun verifyPassword(@Field("password") password: String): ApiResponse<Unit>

    // 用户注销（删除自己账号）
    @DELETE("/api/users/me")
    suspend fun deleteAccount(): ApiResponse<Unit>
}
