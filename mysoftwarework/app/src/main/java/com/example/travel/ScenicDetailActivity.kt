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

class ScenicDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val landscapeId = intent.getStringExtra("landscapeId") ?: ""
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Scaffold(
                    bottomBar = { CommentInputBar(landscapeId = landscapeId, postId = null) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        ScenicDetailContent(landscapeId)
                    }
                }
            }
        }
    }
}

@Composable
fun ScenicDetailContent(landscapeId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var landscape by remember { mutableStateOf<LandscapeResponse?>(null) }
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLiked by remember { mutableStateOf(false) }
    var isFavorited by remember { mutableStateOf(false) }

    LaunchedEffect(landscapeId) {
        try {
            val response = NetworkClient.apiService.getLandscapeById(landscapeId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.success && body.data != null) {
                    landscape = body.data.toLandscapeResponse()
                } else {
                    Toast.makeText(context, body.message ?: "获取景点详情失败", Toast.LENGTH_SHORT).show()
                }
            } else if (body != null) {
                Toast.makeText(context, body.message ?: "景点不存在或未审核", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("ScenicDetail", "Error loading landscape: ${e.message}", e)
            Toast.makeText(context, "网络连接失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // 顶部图片展示
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            AsyncImage(
                model = "https://via.placeholder.com/800x450.png?text=${landscape?.title ?: "Loading"}",
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
                IconButton(onClick = {
                    scope.launch {
                        try {
                            val res = if (isLiked) NetworkClient.apiService.deleteLike(landscapeId)
                                      else NetworkClient.apiService.addLike(LikeRequest("LANDSCAPE", landscapeId = landscapeId))
                            if (res.success) {
                                isLiked = !isLiked
                                Toast.makeText(context, if (isLiked) "已点赞" else "已取消点赞", Toast.LENGTH_SHORT).show()
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
                            val res = if (isFavorited) NetworkClient.apiService.deleteFavorite(landscapeId)
                                      else NetworkClient.apiService.addFavorite(FavoriteRequest("LANDSCAPE", landscapeId = landscapeId))
                            if (res.success) {
                                isFavorited = !isFavorited
                                Toast.makeText(context, if (isFavorited) "已收藏" else "已取消收藏", Toast.LENGTH_SHORT).show()
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

            // 地图显示区域
            Divider(modifier = Modifier.padding(vertical = 20.dp))
            Text(text = "位置地图", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            
            // 地图占位符，显示位置信息（高德地图SDK待网络恢复后启用）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Map",
                        tint = Color.Blue,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = landscape?.address ?: "暂无地址", color = Color.DarkGray)
                    if (landscape?.latitude != null && landscape?.longitude != null) {
                        Text(
                            text = "坐标: ${landscape?.latitude}, ${landscape?.longitude}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 20.dp))
            
            Text(text = "游客评论 (${comments.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            if (comments.isEmpty()) {
                Text("暂无评论，快来抢沙发吧~", modifier = Modifier.padding(vertical = 20.dp), color = Color.Gray)
            } else {
                comments.forEach { comment ->
                    CommentItem(comment)
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
