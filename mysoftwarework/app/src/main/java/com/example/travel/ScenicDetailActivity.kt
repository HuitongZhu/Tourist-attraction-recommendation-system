package com.example.travel

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch
import retrofit2.HttpException

private suspend fun loadPublicLandscapeDetail(landscapeId: String): LandscapeResponse? {
    val api = NetworkClient.apiService
    try {
        val res = api.getLandscapeDetail(landscapeId)
        if (res.success && res.data != null) {
            Log.d(
                "ScenicDetail",
                "landscape-detail ok id=$landscapeId lat=${res.data.latitude} lng=${res.data.longitude}"
            )
            return res.data.toLandscapeResponse()
        }
        Log.w("ScenicDetail", "landscape-detail failed: ${res.message}")
    } catch (e: HttpException) {
        if (e.code() != 404) throw e
        Log.w("ScenicDetail", "landscape-detail 404, fallback to /api/landscapes/{id}")
    }
    try {
        val response = api.getLandscapeById(landscapeId)
        val body = response.body()
        if (response.isSuccessful && body != null && body.success && body.data != null) {
            Log.d(
                "ScenicDetail",
                "landscapes/{id} ok lat=${body.data.latitude} lng=${body.data.longitude}"
            )
            return body.data.toLandscapeResponse()
        }
    } catch (e: HttpException) {
        if (e.code() != 404) throw e
    }
    return null
}

private suspend fun loadAdminLandscapeDetail(landscapeId: String): LandscapeResponse? {
    val api = NetworkClient.apiService
    for (loader in listOf<suspend () -> ApiResponse<LandscapeResponse>>(
        { api.getAdminLandscapeDetail(landscapeId) },
        { api.getAdminReviewLandscapeDetail(landscapeId) }
    )) {
        try {
            val res = loader()
            if (res.success && res.data != null) return res.data
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
        }
    }
    return null
}

class ScenicDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val landscapeId = intent.getStringExtra("landscapeId") ?: ""
        val adminPreview = intent.getBooleanExtra("adminPreview", false)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                ScenicDetailScreen(landscapeId, adminPreview)
            }
        }
    }
}

@Composable
fun ScenicDetailScreen(landscapeId: String, adminPreview: Boolean = false) {
    val scope = rememberCoroutineScope()
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }

    suspend fun loadComments() {
        try {
            val res = NetworkClient.apiService.getComments(landscapeId = landscapeId, size = 200)
            if (res.success) {
                comments = res.data?.content ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("ScenicDetail", "Error loading comments: ${e.message}", e)
        }
    }

    LaunchedEffect(landscapeId) {
        loadComments()
    }

    Scaffold(
        bottomBar = {
            CommentInputBar(
                landscapeId = landscapeId,
                postId = null,
                onCommentPosted = { scope.launch { loadComments() } }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            ScenicDetailContent(
                landscapeId = landscapeId,
                adminPreview = adminPreview,
                comments = comments,
                onCommentDeleted = { scope.launch { loadComments() } }
            )
        }
    }
}

@Composable
fun ScenicDetailContent(
    landscapeId: String,
    adminPreview: Boolean = false,
    comments: List<CommentResponse>,
    onCommentDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var landscape by remember { mutableStateOf<LandscapeResponse?>(null) }
    var detailLoading by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }
    var isFavorited by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(0) }
    var favoriteCount by remember { mutableStateOf(0) }

    LaunchedEffect(landscapeId, adminPreview) {
        detailLoading = true
        try {
            if (adminPreview) {
                val res = loadAdminLandscapeDetail(landscapeId)
                if (res != null) {
                    landscape = res
                    likeCount = res.likeCount
                    favoriteCount = res.favoriteCount
                } else {
                    Toast.makeText(context, "获取景点详情失败", Toast.LENGTH_SHORT).show()
                }
            } else {
                val detail = loadPublicLandscapeDetail(landscapeId)
                if (detail != null) {
                    landscape = detail
                    likeCount = detail.likeCount
                    favoriteCount = detail.favoriteCount
                } else {
                    Toast.makeText(context, "获取景点详情失败", Toast.LENGTH_SHORT).show()
                }
                if (UserSession.isLoggedIn()) {
                    InteractionHelper.loadLandscapeStatus(landscapeId)?.let { status ->
                        isLiked = status.liked
                        isFavorited = status.favorited
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ScenicDetail", "Error loading landscape: ${e.message}", e)
            Toast.makeText(context, "网络连接失败: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            detailLoading = false
        }
    }

    if (detailLoading && landscape == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // 顶部图片展示
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            val imageModel = NetworkClient.mediaUrl(landscape?.imagePath)
                ?: "https://via.placeholder.com/800x450.png?text=${landscape?.title ?: "Loading"}"
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            
            // 右下角悬浮的点赞和收藏
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (!UserSession.isLoggedIn()) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }
                        scope.launch {
                            try {
                                val liked = InteractionHelper.toggleLandscapeLike(landscapeId)
                                if (liked != null) {
                                    isLiked = liked
                                    likeCount += if (isLiked) 1 else -1
                                    Toast.makeText(context, if (isLiked) "已点赞" else "已取消点赞", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.White
                    )
                    Text(
                        text = likeCount.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (!UserSession.isLoggedIn()) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }
                        scope.launch {
                            try {
                                val favorited = InteractionHelper.toggleLandscapeFavorite(landscapeId)
                                if (favorited != null) {
                                    isFavorited = favorited
                                    favoriteCount += if (isFavorited) 1 else -1
                                    Toast.makeText(context, if (isFavorited) "已收藏" else "已取消收藏", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Favorite",
                        tint = if (isFavorited) Color(0xFFFFD700) else Color.White
                    )
                    Text(
                        text = favoriteCount.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = landscape?.title ?: "加载中...", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("详细地点", landscape?.address ?: "暂无")
            InfoRow("景点等级", landscape?.level ?: "暂无")
            InfoRow("开放时间", landscape?.openingTime ?: "暂无")
            InfoRow("联系电话", landscape?.contactPhone ?: "暂无")

            Divider(modifier = Modifier.padding(vertical = 20.dp))
            
            Text(text = "景点介绍", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = landscape?.content ?: "", 
                fontSize = 17.sp, 
                lineHeight = 26.sp,
                color = Color.DarkGray
            )

            Divider(modifier = Modifier.padding(vertical = 20.dp))
            Text(text = "位置地图", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            if (landscape == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFFE8E8E8)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                val detail = landscape!!
                key(detail.id, detail.latitude, detail.longitude) {
                    ScenicMapView(
                        latitude = detail.latitude,
                        longitude = detail.longitude,
                        title = detail.title,
                        address = detail.address,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 20.dp))
            
            Text(text = "游客评论 (${comments.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            if (comments.isEmpty()) {
                Text("暂无评论，快来抢沙发吧~", modifier = Modifier.padding(vertical = 20.dp), color = Color.Gray)
            } else {
                comments.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        onDeleted = onCommentDeleted,
                        onEdited = onCommentDeleted
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
