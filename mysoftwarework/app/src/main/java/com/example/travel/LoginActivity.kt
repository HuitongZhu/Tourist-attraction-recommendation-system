package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启动页为登录页，不自动沿用上次登录态
        NetworkClient.userId = null
        NetworkClient.userName = null
        NetworkClient.userType = null
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        onRegisterClick = {
                            startActivity(Intent(this, RegisterActivity::class.java))
                        },
                        onForgotPasswordClick = {
                            startActivity(Intent(this, ForgotPasswordActivity::class.java))
                        },
                        onLoginClick = { account, password, isSmsMode, smsCode, isAdmin, showError ->
                            lifecycleScope.launch {
                                try {
                                    // 根据选择的身份设置 userType
                                    val userType = if (isAdmin) "1" else "2" // 1 = 管理员, 2 = 普通用户
                                    
                                    val response = if (isSmsMode) {
                                        NetworkClient.apiService.login(account = account, password = null, code = smsCode, loginType = "sms", userType = userType)
                                    } else {
                                        NetworkClient.apiService.login(account = account, password = password, code = null, loginType = "password", userType = userType)
                                    }

                                    if (response.isSuccessful) {
                                        val body = response.body()?.string()
                                        
                                        if (body != null) {
                                            // 检查是否包含成功响应
                                            if (body.contains("\"code\":200")) {
                                                // 提取用户信息
                                                val userId = if (body.contains("\"userId\":\"")) {
                                                    body.substringAfter("\"userId\":\"").substringBefore("\"")
                                                } else {
                                                    ""
                                                }
                                                val userType = body.substringAfter("\"userType\":\"").substringBefore("\"")
                                                val userName = if (body.contains("\"userName\":\"")) {
                                                    body.substringAfter("\"userName\":\"").substringBefore("\"")
                                                } else {
                                                    ""
                                                }
                                                
                                                // 保存用户信息（持久化，下次启动仍可用）
                                                UserSession.save(this@LoginActivity, userId, userName, userType)
                                                
                                                if (userType == "1") {
                                                    // 管理员
                                                    Toast.makeText(this@LoginActivity, "管理员登录成功", Toast.LENGTH_SHORT).show()
                                                    startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                                                } else {
                                                    // 普通用户
                                                    Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                                }
                                                finish()
                                            } else {
                                                // 提取错误消息
                                                val message = if (body.contains("\"message\":\"")) {
                                                    val start = body.indexOf("\"message\":\"") + 11
                                                    val end = body.indexOf("\"", start)
                                                    if (start < end) body.substring(start, end) else "登录失败"
                                                } else {
                                                    "登录失败"
                                                }
                                                showError(message)
                                            }
                                        } else {
                                            showError("登录失败")
                                        }
                                    } else {
                                        showError("账号或密码错误")
                                    }
                                } catch (e: Exception) {
                                    Log.e("LoginActivity", "Network error: ${e.message}", e)
                                    showError("网络异常")
                                }
                            }
                        },
                        onSendSmsClick = { phone, showError ->
                            lifecycleScope.launch {
                                if (phone.isEmpty()) {
                                    showError("请输入手机号")
                                    return@launch
                                }
                                try {
                                    val ok = sendSmsCodeAndShow(
                                        context = this@LoginActivity,
                                        phone = phone,
                                        type = SmsCodeType.LOGIN,
                                        onError = { msg -> showError(msg) }
                                    )
                                    if (!ok) {
                                        // onError 已处理
                                    }
                                } catch (e: Exception) {
                                    showError("网络异常")
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
fun LoginScreen(
    modifier: Modifier = Modifier,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginClick: (String, String, Boolean, String, Boolean, (String) -> Unit) -> Unit,
    onSendSmsClick: (String, (String) -> Unit) -> Unit
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var isSmsMode by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF1A56DB))
        )
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "旅游书", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A56DB))
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "登录", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(30.dp))

        // 登录身份选择
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomRadioButton(selected = !isAdmin, label = "用户登录", onClick = { isAdmin = false })
            Spacer(modifier = Modifier.width(30.dp))
            CustomRadioButton(selected = isAdmin, label = "管理员登录", onClick = { isAdmin = true })
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 登录方式选择
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomRadioButton(selected = !isSmsMode, label = "密码验证", onClick = { isSmsMode = false })
            Spacer(modifier = Modifier.width(30.dp))
            CustomRadioButton(selected = isSmsMode, label = "短信验证", onClick = { isSmsMode = true })
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (!isSmsMode) {
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("手机号 / 账号") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(15.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        } else {
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("手机号") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(15.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = smsCode,
                    onValueChange = { smsCode = it },
                    label = { Text("验证码") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { 
                        errorMessage = ""
                        onSendSmsClick(account) { msg -> 
                            errorMessage = msg 
                        } 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("发送短信", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))
        Row(modifier = Modifier.fillMaxWidth(0.8f), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onForgotPasswordClick) {
                Text(text = "忘记密码", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { 
                errorMessage = ""
                
                // 1. 检查账号是否为空
                if (account.isEmpty()) {
                    errorMessage = "请输入账号"
                    return@Button
                }
                
                // 2. 根据登录模式进行验证
                if (isSmsMode) {
                    // 短信登录模式
                    if (smsCode.isEmpty()) {
                        errorMessage = "请输入验证码"
                        return@Button
                    }
                    // 检查手机号格式（11位数字）
                    if (!account.matches(Regex("^\\d{11}$"))) {
                        errorMessage = "请输入正确的11位手机号"
                        return@Button
                    }
                } else {
                    // 密码登录模式
                    if (password.isEmpty()) {
                        errorMessage = "请输入密码"
                        return@Button
                    }
                    // 检查密码长度（6位及以上）
                    if (password.length < 6) {
                        errorMessage = "密码长度至少需要6位"
                        return@Button
                    }
                }
                
                onLoginClick(account, password, isSmsMode, smsCode, isAdmin) { msg -> 
                    errorMessage = msg 
                } 
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
        ) {
            Text(text = "登录", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = onRegisterClick) {
            Text(text = "还没有账号？立即注册", fontSize = 14.sp)
        }
        
        // 错误消息显示区域
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun CustomRadioButton(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(2.dp, if (selected) Color(0xFF1A56DB) else Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF1A56DB), CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 16.sp, color = if (selected) Color(0xFF1A56DB) else Color.Gray)
    }
}
