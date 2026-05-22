package com.example.travel

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class CommentReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    AdminTopNavBar()
                    Row(modifier = Modifier.fillMaxSize()) {
                        AdminSidebar(selectedModule = "COMMENT")
                        Box(modifier = Modifier.weight(1f).padding(top = 16.dp, end = 16.dp, bottom = 16.dp)) {
                            CommentReviewMainContent()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentReviewMainContent() {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    var comments by remember { mutableStateOf<List<CommentReviewResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(searchText, refreshTrigger) {
        isLoading = true
        try {
            val response = NetworkClient.apiService.getAdminReviewComments(
                keyword = if (searchText.isEmpty()) null else searchText
            )
            if (response.success) {
                comments = response.data ?: emptyList()
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
        Text("评论管理", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search", fontSize = 18.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(24.dp)) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF1A56DB)
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else if (comments.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("暂无数据", color = Color.Gray, fontSize = 18.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(comments, key = { it.commentId }) { comment ->
                    CommentReviewItem(comment = comment, onDeleted = { refreshTrigger++ })
                }
            }
        }
    }
}

@Composable
fun CommentReviewItem(comment: CommentReviewResponse, onDeleted: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${comment.targetType ?: "内容"}：${comment.targetTitle ?: "-"}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "评论人：${comment.userName ?: "未知"}",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = "删除",
                    color = Color.Red,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                try {
                                    val res = NetworkClient.apiService.deleteAdminComment(comment.commentId)
                                    if (res.success) {
                                        Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                                        onDeleted()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            res.message ?: "删除失败",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (_: Exception) {
                                    Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .padding(start = 8.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(text = comment.content, fontSize = 16.sp, color = Color.DarkGray)
            comment.publishTime?.let {
                Text(text = "时间：$it", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
