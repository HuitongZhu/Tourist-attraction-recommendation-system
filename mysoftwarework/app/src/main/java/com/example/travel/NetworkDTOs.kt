package com.example.travel

import com.google.gson.annotations.SerializedName

/**
 * 统一响应格式（适配后端返回的格式）
 */
data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) {
    val success: Boolean get() = code == 200
}

/**
 * 分页响应格式
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

/**
 * 帖子列表响应（后端实际返回的格式）
 */
data class PostListResponse(
    val total: Int,
    val posts: List<PostBackendResponse>
) {
    fun toPostResponses(): List<PostResponse> {
        return posts.map { it.toPostResponse() }
    }
}

/**
 * 帖子响应（后端实际返回的格式）
 */
data class PostBackendResponse(
    val recomId: String,
    val userId: String,
    val title: String?,
    val landscapeId: String?,
    val landscapeTitle: String? = null,
    val tag: String?,
    val content: String,
    val publishTime: String?,
    val auditState: String,
    val likeCount: Int = 0,
    val favoriteCount: Int = 0
) {
    fun toPostResponse(): PostResponse {
        val landscape = if (!landscapeId.isNullOrBlank() && !landscapeTitle.isNullOrBlank()) {
            LandscapeResponse(
                id = landscapeId,
                title = landscapeTitle,
                content = "",
                address = "",
                latitude = null,
                longitude = null,
                contactPhone = null,
                openingTime = null,
                level = null,
                status = "审核通过",
                auditRemark = null,
                publishedAt = null,
                auditedAt = null,
                creator = null
            )
        } else {
            null
        }
        return PostResponse(
            id = recomId,
            title = title ?: "无标题",
            tag = tag,
            content = content,
            imageUrls = null,
            status = auditState,
            auditRemark = null,
            publishedAt = publishTime,
            auditedAt = null,
            landscapeId = landscapeId,
            landscape = landscape,
            author = null,
            likeCount = likeCount,
            favoriteCount = favoriteCount
        )
    }
}

/**
 * 注册请求数据类
 */
data class RegisterRequest(
    val username: String,
    val phone: String,
    val password: String,
    val confirmPassword: String,
    val smsCode: String,
    val realName: String? = null,
    val idNumber: String? = null,
    val gender: String? = null,
    val birthday: String? = null
)

/**
 * 发送验证码请求
 */
data class SmsSendRequest(
    val phone: String
)

/**
 * 发送验证码响应（与后端 SmsCodeResponse 字段一致，可空避免 Gson 解析失败）
 */
data class SmsSendResponse(
    val phone: String? = null,
    val smsCode: String? = null,
    val expiresInSeconds: Int = 300
)

/**
 * 登录请求数据类
 */
data class LoginRequest(
    val account: String,
    val password: String? = null,
    val smsCode: String? = null
)

/**
 * 认证成功返回结果
 */
data class AuthResponse(
    val userId: String,
    val username: String,
    val phone: String?,
    val role: String,
    val token: String
)

/**
 * 用户概要
 */
data class UserSummary(
    val id: String,
    val username: String,
    val role: String
)

/**
 * 完整用户信息响应（与后端 /api/users/me 一致）
 */
data class UserResponse(
    val userId: String?,
    val userName: String?,
    val userType: String?,
    val phoneNumber: String?,
    val realName: String? = null,
    val idNumber: String? = null,
    val gender: String? = null,
    val birthday: String? = null
)

/** 管理员编辑用户请求 */
data class AdminUpdateUserRequest(
    val userName: String? = null,
    val realName: String? = null,
    val phoneNumber: String? = null,
    val idNumber: String? = null,
    val gender: String? = null,
    val birthday: String? = null
)

/**
 * 景点响应（后端实际返回的格式）
 */
data class LandscapeBackendResponse(
    val landscapeId: String,
    val userId: String,
    val title: String,
    val content: String,
    val address: String,
    @SerializedName(value = "landscapeTel", alternate = ["tel"])
    val landscapeTel: String?,
    val openingTime: String?,
    val level: String?,
    val imagePath: String?,
    @SerializedName(value = "latitude", alternate = ["Latitude", "lat"])
    val latitude: Double?,
    @SerializedName(value = "longitude", alternate = ["Longitude", "lng", "lon"])
    val longitude: Double?,
    val auditState: String,
    val publishTime: String?,
    val auditTime: String?,
    val likeCount: Int = 0,
    val favoriteCount: Int = 0
) {
    fun toLandscapeResponse(): LandscapeResponse {
        return LandscapeResponse(
            id = landscapeId,
            title = title,
            content = content,
            address = address,
            latitude = latitude,
            longitude = longitude,
            contactPhone = landscapeTel,
            openingTime = openingTime,
            level = level,
            imagePath = imagePath,
            status = auditState,
            auditRemark = null,
            publishedAt = publishTime,
            auditedAt = auditTime,
            creator = null,
            likeCount = likeCount,
            favoriteCount = favoriteCount
        )
    }
}

/**
 * 景点响应（前端使用）
 */
data class CommentReviewResponse(
    val commentId: String,
    val userId: String?,
    val userName: String?,
    val content: String,
    val publishTime: String?,
    val landscapeId: String?,
    val postId: String?,
    val targetType: String?,
    val targetTitle: String?,
    val auditState: String
)

data class LandscapeResponse(
    @SerializedName("landscapeId")
    val id: String,
    val title: String,
    val content: String,
    val address: String,
    val imagePath: String? = null,
    @SerializedName(value = "latitude", alternate = ["Latitude", "lat"])
    val latitude: Double?,
    @SerializedName(value = "longitude", alternate = ["Longitude", "lng", "lon"])
    val longitude: Double?,
    @SerializedName(value = "landscapeTel", alternate = ["tel"])
    val contactPhone: String?,
    val openingTime: String?,
    val level: String?,
    @SerializedName("auditState")
    val status: String,
    val auditRemark: String?,
    @SerializedName("publishTime")
    val publishedAt: String?,
    @SerializedName("auditTime")
    val auditedAt: String?,
    val creator: UserSummary?,
    val likeCount: Int = 0,
    val favoriteCount: Int = 0
)

/**
 * 景点新建/修改请求
 */
data class LandscapeRequest(
    val title: String? = null,
    val content: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val tel: String? = null,
    val contactPhone: String? = null,
    val openingTime: String? = null,
    val level: String? = null
)

/**
 * 帖子响应
 */
data class PostResponse(
    val id: String,
    val title: String,
    val tag: String?,
    val content: String,
    val imageUrls: List<String>?,
    val status: String,
    val auditRemark: String?,
    val publishedAt: String?,
    val auditedAt: String?,
    val landscapeId: String?,
    val landscape: LandscapeResponse? = null,
    val author: UserSummary?,
    val likeCount: Int = 0,
    val favoriteCount: Int = 0
)

/**
 * 帖子新建请求
 */
data class PostRequest(
    val landscapeId: String? = null,
    val title: String,
    val tag: String? = null,
    val content: String,
    val imageUrls: List<String>? = null
)

/**
 * 评论响应（与后端 CommentResponse 字段一致）
 */
data class CommentResponse(
    val commentId: String,
    val userId: String,
    val userName: String?,
    val content: String,
    val publishTime: String?,
    val landscapeId: String?,
    val postId: String?
)

/**
 * 发表评论请求
 */
data class CommentRequest(
    val landscapeId: String? = null,
    val postId: String? = null,
    val content: String
)

data class CommentUpdateRequest(
    val content: String
)

/**
 * 收藏请求
 */
data class FavoriteRequest(
    val targetType: String, // LANDSCAPE, POST
    val landscapeId: String? = null,
    val postId: String? = null,
    val linkUrl: String? = null
)

/**
 * 点赞请求
 */
data class LikeRequest(
    val targetType: String, // LANDSCAPE, POST
    val landscapeId: String? = null,
    val postId: String? = null,
    val linkUrl: String? = null
)

/**
 * 更新个人资料请求
 */
data class UpdateProfileRequest(
    val realName: String? = null,
    val phoneNumber: String? = null,
    val idNumber: String? = null,
    val gender: String? = null,
    val birthday: String? = null
)

/**
 * 管理员更新用户信息请求
 */
data class UpdateUserInfoRequest(
    val realName: String? = null,
    val idNumber: String? = null,
    val gender: String? = null,
    val birthday: String? = null
)

/**
 * 互动响应
 */
data class InteractionResponse(
    val id: String?,
    val targetType: String?,
    val landscapeId: String?,
    val postId: String?,
    val linkUrl: String?
)

/**
 * 当前用户对景点/帖子的点赞收藏状态
 */
data class InteractionStatusResponse(
    val liked: Boolean = false,
    val favorited: Boolean = false,
    val likeId: String? = null,
    val favoriteId: String? = null
)

/**
 * 高德地理编码返回（与 GET /api/amap/geocode 一致）
 */
data class AmapGeocodeResponse(
    val success: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val message: String? = null
)

data class GeocodeResponse(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

/** 与 Web landscape-detail.html 一致的高德 JS 地图配置 */
data class AmapMapConfigResponse(
    val webJsKey: String?,
    val jsVersion: String?
)

/**
 * 审核请求
 */
data class AuditRequest(
    val approved: Boolean,
    val remark: String? = null
)

/**
 * 用户名检查响应
 */
data class UsernameCheckResponse(
    val available: Boolean,
    val message: String? = null
)

/**
 * 更新用户状态请求
 */
data class UserStatusRequest(
    val status: String // ACTIVE, INACTIVE, BANNED
)
