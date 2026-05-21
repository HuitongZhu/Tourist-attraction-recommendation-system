package com.example.travel

import android.os.Bundle
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
import androidx.compose.material.icons.filled.*
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
                Scaffold(
                    bottomBar = { CommentInputBar(landscapeId = null, postId = postId) }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        PostDetailContent(postId)
                    }
                }
            }
        }
    }
}

@Composable
fun PostDetailContent(postId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLiked by remember { mutableStateOf(false) }
    var isFavorited by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        try {
            val res = NetworkClient.apiService.getPostById(postId)
            if (res.success && res.data != null) {
                post = res.data.toPostResponse()
            }
            
            // 评论接口不存在，暂时跳过
            // val commRes = NetworkClient.apiService.getComments(postId = postId)
            // if (commRes.success) comments = commRes.data?.content ?: emptyList()
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
                    scope.launch {
                        try {
                            val res = if (isLiked) NetworkClient.apiService.deleteLike(postId)
                                      else NetworkClient.apiService.addLike(LikeRequest("POST", postId = postId))
                            if (res.success) {
                                isLiked = !isLiked
                                Toast.makeText(context, if (isLiked) "点赞成功" else "取消点赞", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) { }
                    }
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.White
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        try {
                            val res = if (isFavorited) NetworkClient.apiService.deleteFavorite(postId)
                                      else NetworkClient.apiService.addFavorite(FavoriteRequest("POST", postId = postId))
                            if (res.success) {
                                isFavorited = !isFavorited
                                Toast.makeText(context, if (isFavorited) "收藏成功" else "取消收藏", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) { }
                    }
                }) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Star else Icons.Default.FavoriteBorder,
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
