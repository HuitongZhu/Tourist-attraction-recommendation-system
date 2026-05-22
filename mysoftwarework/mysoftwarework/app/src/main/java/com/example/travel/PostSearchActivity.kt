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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travel.ui.theme.TravelTheme

class PostSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialKeyword = intent.getStringExtra("keyword") ?: ""
        setContent {
            TravelTheme {
                PostSearchScreen(initialKeyword) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun PostSearchScreen(initialKeyword: String, onBack: () -> Unit) {
    var keyword by remember { mutableStateOf(initialKeyword) }
    var selectedTag by remember { mutableStateOf("全部标签") }
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    // 用于触发搜索
    var searchTrigger by remember { mutableStateOf(0) }

    val tags = listOf("全部标签", "攻略", "美食", "拍照")

    LaunchedEffect(keyword, selectedTag, searchTrigger) {
        isLoading = true
        try {
            val response = NetworkClient.apiService.getPosts(
                keyword = if (keyword.isEmpty()) null else keyword,
                tag = if (selectedTag == "全部标签") null else selectedTag,
                status = "PUBLISHED"
            )
            if (response.success && response.data != null) {
                posts = response.data.toPostResponses()
            }
        } catch (e: Exception) {
            // 错误处理
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // 顶部蓝色条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A56DB))
                .topBarSafePadding()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("推荐帖查询", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // 搜索框
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                placeholder = { Text("输入景点名称搜索", fontSize = 14.sp) },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { searchTrigger++ },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                modifier = Modifier.height(52.dp)
            ) {
                Text("搜索")
            }
        }

        // 筛选条件 (模拟原型图)
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("关联景点：", fontSize = 13.sp, color = Color.Gray)
            Surface(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(2.dp)) {
                Text("南京夫子庙", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("状态：", fontSize = 13.sp, color = Color.Gray)
            Surface(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(2.dp)) {
                Text("审核通过", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp)
            }
        }

        // 标签选择
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                val isSelected = selectedTag == tag
                Surface(
                    modifier = Modifier.weight(1f).clickable { selectedTag = tag },
                    color = if (isSelected) Color(0xFF673AB7) else Color(0xFF9575CD),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        tag,
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // 排序和统计
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("找到${posts.size}个符合条件的推荐帖", fontSize = 12.sp, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("最新发布", fontSize = 12.sp, color = Color(0xFF1A56DB), fontWeight = FontWeight.Bold)
                Text("热门优先", fontSize = 12.sp, color = Color.Gray)
                Text("最早发布", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // 结果列表
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts) { post ->
                    PostSearchItem(post)
                }
            }
        }
    }
}

@Composable
fun PostSearchItem(post: PostResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = if (post.imageUrls?.isNotEmpty() == true) post.imageUrls[0] else "https://via.placeholder.com/100",
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = post.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text("128", fontSize = 12.sp, color = Color.Gray) // 模拟数据
                }
                Text(
                    text = "${post.publishedAt?.take(10) ?: "2025-11-20"}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    post.tag?.split(",")?.forEach { t ->
                        Surface(color = Color.Black, shape = RoundedCornerShape(2.dp)) {
                            Text(t, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
                Text(
                    text = post.content,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
