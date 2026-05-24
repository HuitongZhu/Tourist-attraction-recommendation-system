package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
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
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class PostReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    AdminTopNavBar()
                    Row(modifier = Modifier.fillMaxSize()) {
                        AdminSidebar(selectedModule = "POST")
                        Box(modifier = Modifier.weight(1f).padding(top = 16.dp, end = 16.dp, bottom = 16.dp)) {
                            PostReviewMainContent()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostReviewMainContent() {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(AdminReviewFilter.ALL) }

    LaunchedEffect(searchText, refreshTrigger, selectedFilter) {
        isLoading = true
        try {
            val response = NetworkClient.apiService.getAdminReviewPosts(
                filter = selectedFilter,
                keyword = if (searchText.isEmpty()) null else searchText
            )
            if (response.success) {
                posts = (response.data ?: emptyList())
                    .map { it.toPostResponse() }
                    .sortedBy { AdminReviewFilter.auditSortOrder(it.status) }
            } else {
                Toast.makeText(context, response.message ?: "加载失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("推荐帖审核", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        ReviewFilterRow(selectedFilter = selectedFilter, onFilterChange = { selectedFilter = it })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search", fontSize = 18.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, Modifier.size(24.dp)) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF1A56DB)
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无数据", color = Color.Gray, fontSize = 18.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                items(posts, key = { it.id }) { post ->
                    PostReviewItem(post = post, onAudit = { refreshTrigger++ })
                }
            }
        }
    }
}

@Composable
fun PostReviewItem(post: PostResponse, onAudit: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val displayStatus = AdminReviewFilter.displayStatus(post.status)
    val pending = AdminReviewFilter.isPendingStatus(post.status)
    val approved = AdminReviewFilter.isApprovedStatus(post.status)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除推荐帖") },
            text = { Text("确定删除「${post.title}」？删除后不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        try {
                            val res = NetworkClient.apiService.deleteAdminReviewPost(post.id)
                            if (res.success) {
                                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                                onAudit()
                            } else {
                                Toast.makeText(context, res.message ?: "删除失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("删除", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = post.title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    post.tag?.takeIf { it.isNotBlank() }?.let {
                        Text(text = "标签: $it", fontSize = 16.sp, color = Color.Gray)
                    }
                }
                val statusColor = when {
                    pending -> Color(0xFFFF9800)
                    AdminReviewFilter.isApprovedStatus(post.status) -> Color(0xFF2E7D32)
                    post.status == "审核未通过" -> Color.Red
                    else -> Color.Gray
                }
                Text(text = displayStatus, fontSize = 18.sp, color = statusColor, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = post.content, fontSize = 18.sp, color = Color.Gray, lineHeight = 24.sp, maxLines = 4)
            Spacer(modifier = Modifier.height(24.dp))
            // 两行按钮布局：第一行通过、驳回，第二行详情、删除
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 第一行：通过、驳回
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (pending) {
                        // 通过按钮 - 蓝色实心文字按钮
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val res = NetworkClient.apiService.auditPost(
                                            post.id,
                                            AuditRequest(approved = true, remark = "审核通过")
                                        )
                                        if (res.success) {
                                            Toast.makeText(context, "审核通过", Toast.LENGTH_SHORT).show()
                                            onAudit()
                                        } else {
                                            Toast.makeText(context, res.message ?: "操作失败", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
                            shape = RoundedCornerShape(27.dp)
                        ) {
                            Text("通过", fontSize = 18.sp)
                        }
                        // 驳回按钮 - 红色边框文字按钮
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val res = NetworkClient.apiService.auditPost(
                                            post.id,
                                            AuditRequest(approved = false, remark = "审核未通过")
                                        )
                                        if (res.success) {
                                            Toast.makeText(context, "已驳回", Toast.LENGTH_SHORT).show()
                                            onAudit()
                                        } else {
                                            Toast.makeText(context, res.message ?: "操作失败", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Text("驳回", color = Color.Red, fontSize = 18.sp)
                        }
                    } else {
                        // 已审核状态，显示占位
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                // 第二行：详情、删除
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 详情按钮 - 蓝色边框文字按钮
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(context, PostDetailActivity::class.java)
                            intent.putExtra("postId", post.id)
                            intent.putExtra("adminPreview", true)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        border = BorderStroke(1.dp, Color(0xFF1A56DB))
                    ) {
                        Text("详情", color = Color(0xFF1A56DB), fontSize = 18.sp)
                    }
                    // 删除按钮 - 红色实心文字按钮
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("删除", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
