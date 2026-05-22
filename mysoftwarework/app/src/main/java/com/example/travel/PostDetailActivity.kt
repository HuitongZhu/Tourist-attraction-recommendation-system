package com.example.travel

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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

class PostDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val postId = intent.getStringExtra("postId") ?: ""
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                PostDetailScreen(postId)
            }
        }
    }
}

@Composable
fun PostDetailScreen(postId: String) {
    val scope = rememberCoroutineScope()
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }

    suspend fun loadComments() {
        try {
            val res = NetworkClient.apiService.getComments(postId = postId)
            if (res.success) {
                comments = res.data?.content ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("PostDetail", "Error loading comments: ${e.message}", e)
        }
    }

    LaunchedEffect(postId) {
        loadComments()
    }

    Scaffold(
        bottomBar = {
            CommentInputBar(
                landscapeId = null,
                postId = postId,
                onCommentPosted = { scope.launch { loadComments() } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            PostDetailContent(postId = postId, comments = comments)
        }
    }
}

@Composable
fun PostDetailContent(postId: String, comments: List<CommentResponse>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var isLiked by remember { mutableStateOf(false) }
    var isFavorited by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        try {
            val res = NetworkClient.apiService.getPostById(postId)
            if (res.success && res.data != null) {
                post = res.data.toPostResponse()
            }
            if (UserSession.isLoggedIn()) {
                InteractionHelper.loadPostStatus(postId)?.let { status ->
                    isLiked = status.liked
                    isFavorited = status.favorited
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            val imageUrl = post?.imageUrls?.firstOrNull() ?: "https://via.placeholder.com/400x200.png?text=${post?.title ?: "Loading"}"
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            
            // 右下角悬浮按钮组 (点赞和收藏)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (!UserSession.isLoggedIn()) {
                        Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                        return@IconButton
                    }
                    scope.launch {
                        try {
                            val liked = InteractionHelper.togglePostLike(postId)
                            if (liked != null) {
                                isLiked = liked
                                Toast.makeText(context, if (isLiked) "点赞成功" else "取消点赞", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.White
                    )
                }
                IconButton(onClick = {
                    if (!UserSession.isLoggedIn()) {
                        Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                        return@IconButton
                    }
                    scope.launch {
                        try {
                            val favorited = InteractionHelper.togglePostFavorite(postId)
                            if (favorited != null) {
                                isFavorited = favorited
                                Toast.makeText(context, if (isFavorited) "收藏成功" else "取消收藏", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Favorite",
                        tint = if (isFavorited) Color(0xFFFFD700) else Color.White
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post?.title ?: "加载中...", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = post?.content ?: "", fontSize = 16.sp, lineHeight = 24.sp)

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Text(text = "全部评论 (${comments.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            comments.forEach { comment ->
                CommentItem(comment)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
