package com.example.travel

/**
 * 加载/保存个人资料：优先使用 dto 里已部署的 AdminUserController，再尝试其它接口
 */
suspend fun loadUserProfile(): UserResponse? {
    val userId = NetworkClient.userId?.takeIf { it.isNotBlank() } ?: return null

    try {
        val admin = NetworkClient.apiService.getAdminUserDetail(userId)
        if (admin.success && admin.data != null) {
            return admin.data
        }
    } catch (_: Exception) {
    }

    try {
        val app = NetworkClient.apiService.getUserProfile()
        if (app.success && app.data != null) {
            return app.data
        }
    } catch (_: Exception) {
    }

    try {
        val basic = NetworkClient.apiService.getCurrentUser()
        if (basic.success) {
            return basic.data
        }
    } catch (_: Exception) {
    }
    return null
}

suspend fun saveUserProfile(request: UpdateProfileRequest): ApiResponse<UserResponse>? {
    val userId = NetworkClient.userId?.takeIf { it.isNotBlank() } ?: return null
    val adminReq = AdminUpdateUserRequest(
        realName = request.realName,
        phoneNumber = request.phoneNumber,
        idNumber = request.idNumber,
        gender = request.gender,
        birthday = request.birthday
    )

    try {
        val admin = NetworkClient.apiService.updateAdminUserProfile(userId, adminReq)
        if (admin.success) {
            return admin
        }
        if (!admin.message.isNullOrBlank()) {
            return admin
        }
    } catch (_: Exception) {
    }

    return try {
        NetworkClient.apiService.updateProfile(request)
    } catch (_: Exception) {
        null
    }
}
