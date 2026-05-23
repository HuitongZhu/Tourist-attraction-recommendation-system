package com.example.travel

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

class MobileVerifyPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MobileVerifyPasswordScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBackClick = { finish() },
                        onSendCodeClick = { phone ->
                            lifecycleScope.launch {
                                sendSmsCodeAndShow(
                                    context = this@MobileVerifyPasswordActivity,
                                    phone = phone,
                                    type = SmsCodeType.PASSWORD,
                                    onError = { msg ->
                                        Toast.makeText(this@MobileVerifyPasswordActivity, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        onConfirmClick = { phone, code, newPassword ->
                            lifecycleScope.launch {
                                try {
                                    val response = NetworkClient.apiService.resetPasswordBySms(phone, code, newPassword)
                                    if (response.success) {
                                        Toast.makeText(this@MobileVerifyPasswordActivity, "密码修改成功", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this@MobileVerifyPasswordActivity, "修改失败: ${response.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@MobileVerifyPasswordActivity, "网络故障", Toast.LENGTH_SHORT).show()
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
fun MobileVerifyPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSendCodeClick: (String) -> Unit,
    onConfirmClick: (String, String, String) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var verifyCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(0) }

    // 倒计时逻辑
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            kotlinx.coroutines.delay(1000)
            countdown--
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 顶部蓝色条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF1A56DB))
        )
        
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "旅游书", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A56DB))
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "手机号验证修改密码", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(40.dp))

        // 手机号输入
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("请输入手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 验证码输入
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = verifyCode,
                onValueChange = { verifyCode = it },
                label = { Text("输入验证码") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    errorMessage = ""
                    // 验证手机号格式
                    if (!phone.matches(Regex("^\\d{11}$"))) {
                        errorMessage = "请输入正确的11位手机号"
                        return@Button
                    }
                    // 发送验证码
                    onSendCodeClick(phone)
                    countdown = 60
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
                shape = MaterialTheme.shapes.small,
                enabled = countdown == 0
            ) {
                Text(if (countdown > 0) "${countdown}秒" else "获取验证码", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 新密码
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("设置新密码") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 确认密码
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("再次确认新密码") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(20.dp))
        
        // 错误提示
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 确认按钮
        Button(
            onClick = {
                errorMessage = ""
                
                // 1. 验证手机号格式
                if (!phone.matches(Regex("^\\d{11}$"))) {
                    errorMessage = "请输入正确的11位手机号"
                    return@Button
                }
                
                // 2. 验证验证码不为空
                if (verifyCode.isEmpty()) {
                    errorMessage = "请输入验证码"
                    return@Button
                }
                
                // 3. 验证新密码不为空且长度≥6位
                if (newPassword.isEmpty()) {
                    errorMessage = "请输入新密码"
                    return@Button
                }
                if (newPassword.length < 6) {
                    errorMessage = "新密码长度至少需要6位"
                    return@Button
                }
                
                // 4. 验证确认密码不为空且一致
                if (confirmPassword.isEmpty()) {
                    errorMessage = "请再次输入新密码"
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    errorMessage = "两次输入的密码不一致"
                    return@Button
                }
                
                // 调用修改密码接口
                onConfirmClick(phone, verifyCode, newPassword)
            },
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
        ) {
            Text("确认修改", color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(onClick = onBackClick) {
            Text("返回", color = Color.Gray)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MobileVerifyPasswordPreview() {
    TravelTheme {
        MobileVerifyPasswordScreen(
            onBackClick = {},
            onSendCodeClick = {},
            onConfirmClick = { _, _, _ -> }
        )
    }
}
