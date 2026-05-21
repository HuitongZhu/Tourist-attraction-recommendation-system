package com.example.travel

/**
 * 统一响应格式（适配后端返回的格式）
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    // 后端用 code 字段表示状态，这里提供 success 属性方便使用
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
    val tag: String?,
    val content: String,
    val publishTime: String?,
    val auditState: String
) {
    fun toPostResponse(): PostResponse {
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
            author = null
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
 * 发送验证码响应
 */
data class SmsSendResponse(
    val phone: String,
    val smsCode: String,
    val expiresInSeconds: Int
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
 * 完整用户信息响应 (Admin/User Profile)
 */
data class UserResponse(
    val id: String,
    val username: String,
    val phone: String,
    val realName: String?,
    val idNumber: String?,
    val gender: String?,
    val birthday: String?,
    val role: String,
    val status: String,
    val createdAt: String?
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
    val landscapeTel: String?,
    val openingTime: String?,
    val level: String?,
    val imagePath: String?,
    val latitude: Double?,
    val longitude: Double?,
    val auditState: String,
    val publishTime: String?,
    val auditTime: String?
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
            status = auditState,
            auditRemark = null,
            publishedAt = publishTime,
            auditedAt = auditTime,
            creator = null
        )
    }
}

/**
 * 景点响应（前端使用）
 */
data class LandscapeResponse(
    val id: String,
    val title: String,
    val content: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val contactPhone: String?,
    val openingTime: String?,
    val level: String?,
    val status: String,
    val auditRemark: String?,
    val publishedAt: String?,
    val auditedAt: String?,
    val creator: UserSummary?
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
    val author: UserSummary?
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
 * 评论响应
 */
data class CommentResponse(
    val id: String,
    val content: String,
    val targetType: String,
    val status: String,
    val auditRemark: String?,
    val publishedAt: String?,
    val auditedAt: String?,
    val landscapeId: String?,
    val postId: String?,
    val author: UserSummary?
)

/**
 * 发表评论请求
 */
data class CommentRequest(
    val landscapeId: String? = null,
    val postId: String? = null,
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
    val id: String,
    val targetType: String,
    val landscapeId: String?,
    val postId: String?,
    val linkUrl: String?
)

/**
 * 地理编码请求
 */
data class GeocodeRequest(
    val address: String
)

/**
 * 地理编码返回结果
 */
data class GeocodeResponse(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val provider: String
)

/**
 * 审核请求
 */
data class AuditRequest(
    val approved: Boolean,
    val remark: String? = null
)
