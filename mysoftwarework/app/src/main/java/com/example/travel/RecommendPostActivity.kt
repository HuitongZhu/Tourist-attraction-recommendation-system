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
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    var searchText by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    suspend fun fetchLandscapeTitle(landscapeId: String): String? {
        return try {
            val response = NetworkClient.apiService.getLandscapeById(landscapeId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.title
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun performSearch(keyword: String = searchText, showFullLoading: Boolean = true) {
        if (showFullLoading) isLoading = true
        try {
            val response = NetworkClient.apiService.getPosts(
                keyword = if (keyword.isBlank()) null else keyword,
                status = "审核通过"
            )
            if (response.success && response.data != null) {
                var postList = response.data.toPostResponses()
                postList = postList.map { post ->
                    if (!post.landscapeId.isNullOrBlank() && post.landscape == null) {
                        val landscapeTitle = fetchLandscapeTitle(post.landscapeId)
                        if (landscapeTitle != null) {
                            post.copy(
                                landscape = LandscapeResponse(
                                    id = post.landscapeId,
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
                            )
                        } else {
                            post
                        }
                    } else {
                        post
                    }
                }
                posts = postList
            } else {
                posts = emptyList()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "搜索失败: ${e.message}", Toast.LENGTH_SHORT).show()
            posts = emptyList()
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        performSearch("")
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                performSearch(showFullLoading = false)
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        item {
            Text(text = "推荐贴", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // 搜索框
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("搜索推荐帖或景点名称...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        IconButton(onClick = { scope.launch { performSearch(searchText) } }) {
                            Text("搜索", color = Color.Blue, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading && posts.isEmpty()) {
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
                            // 显示tag标签
                            if (!post.tag.isNullOrBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    post.tag.split(",").forEach { tag ->
                                        val trimmedTag = tag.trim()
                                        if (trimmedTag.isNotEmpty()) {
                                            Text(
                                                text = "#$trimmedTag",
                                                fontSize = 12.sp,
                                                color = Color(0xFF0066CC)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (post.landscape != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "📍", fontSize = 12.sp)
                                    Text(
                                        text = post.landscape.title,
                                        fontSize = 12.sp,
                                        color = Color(0xFF009966)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            Text(text = post.content, color = Color.Gray, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
    }
}
