package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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

class RecommendPostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopNavBar(
                        currentPage = PageType.RECOMMEND,
                        onPageChange = { page ->
                            when (page) {
                                PageType.HOME -> startActivity(Intent(this@RecommendPostActivity, HomeActivity::class.java))
                                PageType.PUBLISH_SCENIC -> startActivity(Intent(this@RecommendPostActivity, PublishScenicInfoActivity::class.java))
                                PageType.PUBLISH_POST -> startActivity(Intent(this@RecommendPostActivity, PublishPostActivity::class.java))
                                PageType.PERSONAL -> startActivity(Intent(this@RecommendPostActivity, PersonalHomeActivity::class.java))
                                else -> {}
                            }
                        }
                    )
                    RecommendScreen()
                }
            }
        }
    }
}

@Composable
fun RecommendScreen() {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // 获取审核通过的推荐帖
            val response = NetworkClient.apiService.getPosts(status = "审核通过")
            if (response.success && response.data != null) {
                posts = response.data.toPostResponses()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "获取推荐帖失败", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "推荐贴", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // 搜索框入口
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .clickable {
                        val intent = Intent(context, PostSearchActivity::class.java)
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("搜索推荐帖或景点名称...", color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (posts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("暂无推荐帖", color = Color.Gray)
                }
            }
        } else {
            items(posts) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(context, PostDetailActivity::class.java)
                            intent.putExtra("postId", post.id)
                            context.startActivity(intent)
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        val imageUrl = if (!post.imageUrls.isNullOrEmpty()) post.imageUrls!![0] else "https://via.placeholder.com/400x200.png?text=${post.title}"
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = post.title,
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = post.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = post.content, color = Color.Gray, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}
