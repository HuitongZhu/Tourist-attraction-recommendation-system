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
import androidx.compose.ui.window.Dialog
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
                    AdminTopNavBar(onProfileClick = { currentModule = "PROFILE" })

                    Row(modifier = Modifier.fillMaxSize()) {
                        AdminSidebar(
                            selectedModule = currentModule,
                            onModuleSelect = { currentModule = it }
                        )

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

        UserDataTable(searchText, refreshTrigger) { refreshTrigger++ }
    }
}

@Composable
fun UserDataTable(filter: String, refreshTrigger: Int, onRefresh: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var editingUserId by remember { mutableStateOf<String?>(null) }
    var users by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(filter, refreshTrigger) {
        isLoading = true
        try {
            val response = NetworkClient.apiService.getAllUsers(keyword = if (filter.isEmpty()) null else filter)
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(users, key = { it.userId ?: "" }) { user ->
                UserListItem(
                    user = user,
                    onEdit = { editingUserId = user.userId },
                    onDelete = {
                        scope.launch {
                            try {
                                val res = NetworkClient.apiService.deleteUser(user.userId ?: "")
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

    editingUserId?.let { userId ->
        EditUserDialog(
            userId = userId,
            onDismiss = { editingUserId = null },
            onSaved = {
                editingUserId = null
                onRefresh()
            }
        )
    }
}

@Composable
fun UserListItem(user: UserResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
    val name = user.userName.orEmpty()
    val isAdmin = user.userType == "1"
    val roleLabel = if (isAdmin) "系统管理员" else "普通用户"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name.ifBlank { "未命名用户" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "修改信息",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                UserInfoLine("用户 ID", user.userId)
                UserInfoLine("用户类型", roleLabel, valueColor = if (isAdmin) Color.Red else Color(0xFF4A90E2))
                user.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                    UserInfoLine("联系电话", it)
                }
                user.realName?.takeIf { it.isNotBlank() }?.let {
                    UserInfoLine("真实姓名", it)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF1A56DB))
                ) {
                    Text("修改信息", color = Color(0xFF1A56DB), fontSize = 16.sp)
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoLine(label: String, value: String?, valueColor: Color = Color.DarkGray) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label：",
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier.width(88.dp)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(userId: String, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("男") }
    var birthday by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    val genderOptions = listOf("男", "女")

    LaunchedEffect(userId) {
        loading = true
        try {
            val res = NetworkClient.apiService.getAdminUserDetail(userId)
            if (res.success && res.data != null) {
                val d = res.data
                userName = d.userName.orEmpty()
                realName = d.realName.orEmpty()
                phoneNumber = d.phoneNumber.orEmpty()
                idNumber = d.idNumber.orEmpty()
                gender = d.gender?.takeIf { it.isNotBlank() } ?: "男"
                birthday = d.birthday.orEmpty()
            } else {
                Toast.makeText(context, res.message ?: "加载用户信息失败", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            onDismiss()
        } finally {
            loading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("编辑用户", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    AdminEditField("用户名", userName) { userName = it }
                    AdminEditField("真实姓名", realName) { realName = it }
                    AdminEditField("联系电话", phoneNumber) { phoneNumber = it }
                    AdminEditField("身份证号", idNumber) { idNumber = it }

                    Text("性别", fontSize = 14.sp, color = Color.Gray)
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    AdminEditField("生日", birthday, placeholder = "如 1997-10-02") { birthday = it }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                if (userName.isBlank()) {
                                    Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                saving = true
                                scope.launch {
                                    try {
                                        val req = AdminUpdateUserRequest(
                                            userName = userName.trim(),
                                            realName = realName.trim().ifBlank { null },
                                            phoneNumber = phoneNumber.trim().ifBlank { null },
                                            idNumber = idNumber.trim().ifBlank { null },
                                            gender = gender,
                                            birthday = birthday.trim().ifBlank { null }
                                        )
                                        val res = NetworkClient.apiService.updateAdminUserProfile(userId, req)
                                        if (res.success) {
                                            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                                            onSaved()
                                        } else {
                                            Toast.makeText(context, res.message ?: "保存失败", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        saving = false
                                    }
                                }
                            },
                            enabled = !saving,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Text(if (saving) "保存中…" else "保存")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminEditField(
    label: String,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = label != "身份证号",
            shape = RoundedCornerShape(8.dp)
        )
    }
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
