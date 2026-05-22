package com.example.travel

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * 共享的表单输入框
 */
@Composable
fun FormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

/**
 * 共享的信息行展示
 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ", color = Color.Gray, fontSize = 15.sp)
        Text(text = value, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * 共享的评论项展示
 */
@Composable
fun CommentItem(comment: CommentResponse) {
    val displayName = comment.userName?.takeIf { it.isNotBlank() } ?: "匿名用户"
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = comment.publishTime?.take(16) ?: "刚刚", fontSize = 12.sp, color = Color.LightGray)
            }
        }
        Text(
            text = comment.content, 
            modifier = Modifier.padding(start = 46.dp, top = 4.dp), 
            fontSize = 15.sp,
            color = Color.Black
        )
    }
}

/**
 * 共享的底部评论输入栏
 */
@Composable
fun CommentInputBar(
    landscapeId: String?,
    postId: String?,
    onCommentPosted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var commentText by remember { mutableStateOf("") }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("写下你的评论...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A56DB),
                    unfocusedContainerColor = Color(0xFFF5F5F5)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (commentText.isBlank()) return@IconButton
                    // 验证landscapeId或postId是否有效
                    if (landscapeId.isNullOrBlank() && postId.isNullOrBlank()) {
                        Toast.makeText(context, "无法获取评论目标ID", Toast.LENGTH_SHORT).show()
                        return@IconButton
                    }
                    // 验证landscapeId不为"0"（无效ID）
                    if (!landscapeId.isNullOrBlank() && landscapeId == "0") {
                        Toast.makeText(context, "景点ID无效", Toast.LENGTH_SHORT).show()
                        return@IconButton
                    }
                    scope.launch {
                        try {
                            val res = NetworkClient.apiService.createComment(CommentRequest(landscapeId, postId, commentText))
                            if (res.success) {
                                Toast.makeText(context, "评论发表成功", Toast.LENGTH_SHORT).show()
                                commentText = ""
                                onCommentPosted?.invoke()
                            } else {
                                Toast.makeText(context, res.message ?: "评论提交失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            // 评论接口暂未开放，显示友好提示
                            Toast.makeText(context, "评论功能暂未开放", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = commentText.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF1A56DB))
            }
        }
    }
}
