package com.example.travel

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextDecoration
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

private val commentActionGreen = Color(0xFF2E7D32)

private suspend fun submitCommentUpdate(commentId: String, content: String): ApiResponse<CommentResponse>? {
    val api = NetworkClient.apiService
    val body = CommentUpdateRequest(content)
    for (call in listOf<suspend () -> ApiResponse<CommentResponse>>(
        { api.updateMyComment(commentId, body) },
        { api.updateUserComment(commentId, body) }
    )) {
        try {
            val res = call()
            if (res.success) return res
        } catch (_: Exception) {
            // try next path
        }
    }
    return null
}

/**
 * 共享的评论项展示（本人评论右侧：绿色「编辑」、红色「删除」，均可点击）
 */
@Composable
fun CommentItem(
    comment: CommentResponse,
    onDeleted: (() -> Unit)? = null,
    onEdited: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val displayName = comment.userName?.takeIf { it.isNotBlank() } ?: "匿名用户"
    val currentUserId = NetworkClient.userId
    val isOwnComment = !currentUserId.isNullOrBlank() &&
            !comment.userId.isNullOrBlank() &&
            currentUserId == comment.userId
    var showEditDialog by remember { mutableStateOf(false) }
    var editText by remember(comment.commentId, comment.content) { mutableStateOf(comment.content) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("编辑评论") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 6
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val text = editText.trim()
                        if (text.isBlank()) {
                            Toast.makeText(context, "评论内容不能为空", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        showEditDialog = false
                        scope.launch {
                            try {
                                val res = submitCommentUpdate(comment.commentId, text)
                                if (res?.success == true) {
                                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
                                    onEdited?.invoke() ?: onDeleted?.invoke()
                                } else {
                                    Toast.makeText(
                                        context,
                                        res?.message ?: "修改失败",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (_: Exception) {
                                Toast.makeText(context, "修改失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = comment.publishTime?.take(16) ?: "刚刚", fontSize = 12.sp, color = Color.LightGray)
            }
            if (isOwnComment && (onEdited != null || onDeleted != null)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (onEdited != null) {
                        Text(
                            text = "编辑",
                            color = commentActionGreen,
                            fontSize = 13.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                editText = comment.content
                                showEditDialog = true
                            }
                        )
                    }
                    if (onDeleted != null) {
                        Text(
                            text = "删除",
                            color = Color.Red,
                            fontSize = 13.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                scope.launch {
                                    try {
                                        val res = NetworkClient.apiService.deleteComment(comment.commentId)
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
                        )
                    }
                }
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
