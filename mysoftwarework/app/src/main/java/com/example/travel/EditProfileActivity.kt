package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                // 1. 定义状态变量
                var username by remember { mutableStateOf("") }
                var realName by remember { mutableStateOf("") }
                var gender by remember { mutableStateOf("") }
                var birthday by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }

                // 2. 进入页面时同步后端数据
                LaunchedEffect(Unit) {
                    try {
                        val response = NetworkClient.apiService.getCurrentUser()
                        if (response.success && response.data != null) {
                            val user = response.data
                            username = user.username
                            realName = user.realName ?: ""
                            gender = user.gender ?: ""
                            birthday = user.birthday ?: ""
                            phone = user.phone
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@EditProfileActivity, "获取个人资料失败", Toast.LENGTH_SHORT).show()
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    TopNavBar(
                        currentPage = PageType.PERSONAL,
                        onPageChange = { page ->
                            when (page) {
                                PageType.HOME -> startActivity(Intent(this@EditProfileActivity, HomeActivity::class.java))
                                PageType.RECOMMEND -> startActivity(Intent(this@EditProfileActivity, RecommendPostActivity::class.java))
                                PageType.PUBLISH_SCENIC -> startActivity(Intent(this@EditProfileActivity, PublishScenicInfoActivity::class.java))
                                PageType.PUBLISH_POST -> startActivity(Intent(this@EditProfileActivity, PublishPostActivity::class.java))
                                PageType.PERSONAL -> finish()
                            }
                        }
                    )

                    // 内容区
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("修改个人资料", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(20.dp))

                        // 输入框同步状态
                        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("用户名") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = realName, onValueChange = { realName = it }, label = { Text("真实姓名") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("性别") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = birthday, onValueChange = { birthday = it }, label = { Text("生日") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("手机号") }, modifier = Modifier.fillMaxWidth(), readOnly = true)

                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                            onClick = {
                                // 3. 点击保存，将数据同步回数据库
                                lifecycleScope.launch {
                                    try {
                                        // 修改资料通常不需要再次验证短信或密码
                                        val req = UpdateProfileRequest(
                                            realName = realName.ifEmpty { null },
                                            gender = gender.ifEmpty { null },
                                            birthday = birthday.ifEmpty { null }
                                        )
                                        val res = NetworkClient.apiService.updateProfile(req)
                                        if (res.success) {
                                            Toast.makeText(this@EditProfileActivity, "资料已更新", Toast.LENGTH_SHORT).show()
                                            finish()
                                        } else {
                                            Toast.makeText(this@EditProfileActivity, "更新失败: ${res.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(this@EditProfileActivity, "网络故障", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
                        ) {
                            Text("保存修改")
                        }
                    }
                }
            }
        }
    }
}
