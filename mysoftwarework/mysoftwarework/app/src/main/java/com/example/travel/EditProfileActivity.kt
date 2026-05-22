package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                var username by remember { mutableStateOf("") }
                var realName by remember { mutableStateOf("") }
                var gender by remember { mutableStateOf("") }
                var birthday by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }
                var idNumber by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    try {
                        val user = loadUserProfile()
                        if (user != null) {
                            username = user.userName ?: ""
                            realName = user.realName ?: ""
                            gender = user.gender ?: ""
                            birthday = user.birthday ?: ""
                            phone = user.phoneNumber ?: ""
                            idNumber = user.idNumber ?: ""
                        } else {
                            Toast.makeText(this@EditProfileActivity, "获取个人资料失败", Toast.LENGTH_SHORT).show()
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
                                else -> {}
                            }
                        }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("编辑个人资料", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = {},
                            label = { Text("用户名") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        OutlinedTextField(
                            value = realName,
                            onValueChange = { realName = it },
                            label = { Text("真实姓名") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("性别") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = birthday,
                            onValueChange = { birthday = it },
                            label = { Text("生日 (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("手机号") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = idNumber,
                            onValueChange = { idNumber = it },
                            label = { Text("身份证号") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (realName.isBlank() && gender.isBlank() && birthday.isBlank()
                                    && phone.isBlank() && idNumber.isBlank()
                                ) {
                                    Toast.makeText(this@EditProfileActivity, "请至少修改一项信息", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                lifecycleScope.launch {
                                    try {
                                        val req = UpdateProfileRequest(
                                            realName = realName.trim().ifEmpty { null },
                                            gender = gender.trim().ifEmpty { null },
                                            birthday = birthday.trim().ifEmpty { null },
                                            phoneNumber = phone.trim().ifEmpty { null },
                                            idNumber = idNumber.trim().ifEmpty { null }
                                        )
                                        val res = saveUserProfile(req)
                                        if (res != null && res.success) {
                                            Toast.makeText(this@EditProfileActivity, "资料已更新", Toast.LENGTH_SHORT).show()
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this@EditProfileActivity,
                                                res?.message ?: "保存失败，请重启后端后重试",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
