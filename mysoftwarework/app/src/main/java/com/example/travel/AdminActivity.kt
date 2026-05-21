package com.example.travel

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.lifecycleScope
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                var currentModule by remember { mutableStateOf("USER_MGMT") }

                Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    // 1. 顶部导航栏
                    AdminTopNavBar(onProfileClick = { currentModule = "PROFILE" })

                    Row(modifier = Modifier.fillMaxSize()) {
                        // 2. 左侧导航侧边栏
                        AdminSidebar(
                            selectedModule = currentModule,
                            onModuleSelect = { currentModule = it }
                        )

                        // 3. 右侧主内容区
                        Box(modifier = Modifier.weight(1f).padding(top = 16.dp, end = 16.dp, bottom = 16.dp)) {
                            when (currentModule) {
                                "USER_MGMT" -> UserManagementScreen()
                                "PROFILE" -> AdminProfilePage(onBack = { currentModule = "USER_MGMT" })
                                else -> UserManagementScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserManagementScreen() {
    var searchText by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "用户信息",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 搜索框
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search", fontSize = 18.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp)) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF1A56DB)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 用户列表
        UserDataTable(searchText, refreshTrigger) { refreshTrigger++ }
    }
}

@Composable
fun UserDataTable(filter: String, refreshTrigger: Int, onRefresh: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUser by remember { mutableStateOf<UserResponse?>(null) }
    var users by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // 从后端加载用户
    LaunchedEffect(filter, refreshTrigger) {
        isLoading = true
        try {
            val response = NetworkClient.apiService.getAllUsers(keyword = if(filter.isEmpty()) null else filter)
            if (response.success) {
                users = response.data?.content ?: emptyList()
            } else {
                Toast.makeText(context, "加载失败: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(users) { user ->
                UserListItem(
                    user = user,
                    onEdit = { selectedUser = user },
                    onDelete = {
                        scope.launch {
                            try {
                                val res = NetworkClient.apiService.deleteUser(user.id)
                                if (res.success) {
                                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                                    onRefresh()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }

    // 修改信息弹窗
    selectedUser?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { selectedUser = null },
            onSave = { updatedStatus ->
                scope.launch {
                    try {
                        val res = NetworkClient.apiService.updateUserStatus(user.id, updatedStatus)
                        if (res.success) {
                            Toast.makeText(context, "状态已更新", Toast.LENGTH_SHORT).show()
                            selectedUser = null
                            onRefresh()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "同步失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
fun UserListItem(user: UserResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
    val initial = if (user.username.isNotEmpty()) user.username.take(1) else "?"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF4A90E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "ID: ${user.id}",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if(user.role == "ADMIN") "系统管理员" else "普通用户",
                        fontSize = 18.sp,
                        color = if(user.role == "ADMIN") Color.Red else Color(0xFF4A90E2),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "状态: ${user.status}",
                        fontSize = 16.sp,
                        color = if(user.status == "正常") Color(0xFF2E7D32) else Color.Red
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("修改状态", color = Color.Gray, fontSize = 18.sp)
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("删除用户", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(user: UserResponse, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var status by remember { mutableStateOf(user.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改用户状态", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("用户: ${user.username}", fontSize = 18.sp)
                Column {
                    Text("账号状态", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = status == "正常", onClick = { status = "正常" })
                        Text("正常", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = status == "禁用", onClick = { status = "禁用" })
                        Text("禁用", fontSize = 18.sp)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(status) }) { Text("确认修改", fontSize = 18.sp) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", fontSize = 18.sp) } }
    )
}

@Composable
fun AdminProfilePage(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Text("← 返回管理台", color = Color(0xFF1A56DB), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(110.dp).background(Color(0xFF1E293B), CircleShape), contentAlignment = Alignment.Center) {
                    Text("A", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("系统管理员", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Super Admin", color = Color.Gray, fontSize = 20.sp)
                Divider(modifier = Modifier.padding(vertical = 32.dp), color = Color(0xFFF3F4F6))
                AdminInfoItem("后台账号", "admin")
                AdminInfoItem("最后登录", "2024-03-21 14:30")
                AdminInfoItem("系统版本", "v2.1.0")
            }
        }
    }
}

@Composable
fun AdminInfoItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF757575), fontSize = 20.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
    }
}
