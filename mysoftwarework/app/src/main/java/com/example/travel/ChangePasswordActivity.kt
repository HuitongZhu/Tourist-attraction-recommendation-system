package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class ChangePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChangePasswordScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBackClick = { finish() },
                        onMobileVerifyClick = {
                            startActivity(Intent(this, MobileVerifyPasswordActivity::class.java))
                        },
                        onConfirmClick = { oldPassword, newPassword ->
                            // 此处添加原密码校验及新密码保存逻辑
                            lifecycleScope.launch {
                                try {
                                    val response = NetworkClient.apiService.changePassword(oldPassword, newPassword)
                                    if (response.success) {
                                        Toast.makeText(this@ChangePasswordActivity, "密码修改成功", Toast.LENGTH_SHORT).show()
                                        // 跳转到个人中心页面
                                        val intent = Intent(this@ChangePasswordActivity, PersonalHomeActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this@ChangePasswordActivity, "修改失败: ${response.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@ChangePasswordActivity, "网络故障", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onMobileVerifyClick: () -> Unit,
    onConfirmClick: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 顶部蓝色装饰条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF1A56DB))
        )
        
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "旅游书", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A56DB))
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "修改密码", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(40.dp))
        
        // 原密码
        PasswordTextField(
            value = oldPassword,
            onValueChange = { oldPassword = it },
            label = "请输入原密码",
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // 新密码
        PasswordTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = "设置新密码",
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // 确认新密码
        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "再次输入新密码",
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(20.dp))
        
        // 错误提示
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Button(
            onClick = {
                errorMessage = ""
                
                // 1. 验证原密码不为空
                if (oldPassword.isEmpty()) {
                    errorMessage = "请输入原密码"
                    return@Button
                }
                
                // 2. 验证新密码不为空且长度≥6位
                if (newPassword.isEmpty()) {
                    errorMessage = "请输入新密码"
                    return@Button
                }
                if (newPassword.length < 6) {
                    errorMessage = "新密码长度至少需要6位"
                    return@Button
                }
                
                // 3. 验证确认密码不为空且与新密码一致
                if (confirmPassword.isEmpty()) {
                    errorMessage = "请再次输入新密码"
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    errorMessage = "两次输入的密码不一致"
                    return@Button
                }
                
                // 4. 调用修改密码接口
                onConfirmClick(oldPassword, newPassword)
            },
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "确认", fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // 切换到手机号验证逻辑
        TextButton(onClick = onMobileVerifyClick) {
            Text(text = "手机号验证修改 >", fontSize = 14.sp, color = Color(0xFF1A56DB))
        }

        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(onClick = onBackClick, modifier = Modifier.padding(bottom = 32.dp)) {
            Text(text = "取消修改", color = Color.Gray)
        }
    }
}
