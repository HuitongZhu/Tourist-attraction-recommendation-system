package com.example.travel

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class MyPostsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                MyPostsScreen(
                    activity = this,
                    onBack = { finish() }
                )
            }
        }
    }
}

private fun postStatusColor(status: String): Color = when (status) {
    "审核通过" -> Color(0xFF4CAF50)
    "审核未通过" -> Color(0xFFE91E63)
    "待审核", "审核中" -> Color(0xFFFF9800)
    else -> Color.Gray
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(
    activity: ComponentActivity?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var posts by remember { mutableStateOf<List<PostBackendResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var expandedId by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<PostBackendResponse?>(null) }

    suspend fun reload() {
        loading = true
        try {
            val res = NetworkClient.apiService.getMyPosts()
            if (res.success) {
                posts = res.data ?: emptyList()
            } else {
                Toast.makeText(context, res.message ?: "加载失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        if (!UserSession.isLoggedIn()) {
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
            loading = false
            return@LaunchedEffect
        }
        reload()
    }

    if (deleteTarget != null) {
        val title = deleteTarget?.title?.takeIf { it.isNotBlank() }
            ?: deleteTarget?.tag?.takeIf { it.isNotBlank() }
            ?: "该推荐帖"
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除推荐帖") },
            text = { Text("确定删除「$title」？删除后不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    val id = deleteTarget?.recomId ?: return@TextButton
                    deleteTarget = null
                    activity?.lifecycleScope?.launch {
                        try {
                            val res = NetworkClient.apiService.deleteMyPost(id)
                            if (res.success) {
                                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                                reload()
                            } else {
                                Toast.makeText(context, res.message ?: "删除失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("删除", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
                title = { Text("我的推荐帖") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                posts.isEmpty() -> Text(
                    text = "暂无发布的推荐帖",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(posts, key = { it.recomId }) { post ->
                        MyPostListItem(
                            post = post,
                            expanded = expandedId == post.recomId,
                            onToggle = {
                                expandedId = if (expandedId == post.recomId) null else post.recomId
                            },
                            onDelete = { deleteTarget = post }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPostListItem(
    post: PostBackendResponse,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = postStatusColor(post.auditState)
    val displayTitle = post.title?.takeIf { it.isNotBlank() } ?: post.tag ?: "无标题"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    displayTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Surface(color = statusColor, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        post.auditState,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            post.landscapeTitle?.takeIf { it.isNotBlank() }?.let {
                Text("关联景点：$it", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
            post.tag?.takeIf { it.isNotBlank() }?.let {
                Text("标签：$it", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
            }
            post.publishTime?.let {
                Text("发布时间：$it", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
            ) {
                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(post.content, fontSize = 14.sp, color = Color.DarkGray)
                    Text(
                        text = "点击收起",
                        fontSize = 12.sp,
                        color = Color(0xFF4A90E2),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        post.content,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "点击查看全文",
                        fontSize = 12.sp,
                        color = Color(0xFF4A90E2),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDelete) {
                    Text("删除", color = Color.Red)
                }
            }
        }
    }
}
