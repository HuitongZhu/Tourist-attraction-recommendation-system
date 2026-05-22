package com.example.travel

/**
 * 点赞/收藏：后端 POST 接口为 toggle，返回 data.id 非空表示已点赞/已收藏。
 */
object InteractionHelper {

    suspend fun loadLandscapeStatus(landscapeId: String): InteractionStatusResponse? {
        val res = NetworkClient.apiService.getInteractionStatus(landscapeId = landscapeId)
        return if (res.success) res.data else null
    }

    suspend fun loadPostStatus(postId: String): InteractionStatusResponse? {
        val res = NetworkClient.apiService.getInteractionStatus(postId = postId)
        return if (res.success) res.data else null
    }

    /** @return true=已点赞, false=已取消, null=失败 */
    suspend fun toggleLandscapeLike(landscapeId: String): Boolean? {
        val res = NetworkClient.apiService.addLike(LikeRequest("LANDSCAPE", landscapeId = landscapeId))
        if (!res.success) return null
        return res.data?.id != null
    }

    suspend fun toggleLandscapeFavorite(landscapeId: String): Boolean? {
        val res = NetworkClient.apiService.addFavorite(FavoriteRequest("LANDSCAPE", landscapeId = landscapeId))
        if (!res.success) return null
        return res.data?.id != null
    }

    suspend fun togglePostLike(postId: String): Boolean? {
        val res = NetworkClient.apiService.addLike(LikeRequest("POST", postId = postId))
        if (!res.success) return null
        return res.data?.id != null
    }

    suspend fun togglePostFavorite(postId: String): Boolean? {
        val res = NetworkClient.apiService.addFavorite(FavoriteRequest("POST", postId = postId))
        if (!res.success) return null
        return res.data?.id != null
    }
}
