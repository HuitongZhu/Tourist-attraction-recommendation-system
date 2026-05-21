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
                            Toast.makeText(this, "功能暂未开放", Toast.LENGTH_SHORT).show()
                        },
                        onLoginClick = { account, password, isSmsMode, smsCode, showError ->
                            lifecycleScope.launch {
                                try {
                                    val response = if (isSmsMode) {
                                        if (account.isEmpty() || smsCode.isEmpty()) {
                                            showError("请输入手机号和验证码")
                                            return@launch
                                        }
                                        NetworkClient.apiService.login(account = account, password = null, code = smsCode, loginType = "sms")
                                    } else {
                                        if (account.isEmpty() || password.isEmpty()) {
                                            showError("请输入账号和密码")
                                            return@launch
                                        }
                                        NetworkClient.apiService.login(account = account, password = password, code = null, loginType = "password")
                                    }

                                    if (response.isSuccessful) {
                                        val body = response.body()?.string()
                                        
                                        if (body != null) {
                                            // 解析 JSON 响应
                                            try {
                                                val jsonObject = org.json.JSONObject(body)
                                                val success = jsonObject.getBoolean("success")
                                                
                                                if (success) {
                                                    val data = jsonObject.getJSONObject("data")
                                                    val userType = data.getString("userType")
                                                    val userId = data.getString("userId")
                                                    val userName = data.getString("userName")
                                                    
                                                    // 保存用户信息
                                                    NetworkClient.userToken = userId
                                                    NetworkClient.userId = userId
                                                    NetworkClient.userName = userName
                                                    NetworkClient.userType = userType
                                                    
                                                    // 根据用户类型跳转
                                                    if (userType == "1") {
                                                        Toast.makeText(this@LoginActivity, "管理员登录成功", Toast.LENGTH_SHORT).show()
                                                        startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                                                    } else {
                                                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                                                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                                    }
                                                    finish()
                                                } else {
                                                    val message = jsonObject.optString("message", "登录失败")
                                                    showError(message)
                                                }
                                            } catch (e: Exception) {
                                                Log.e("LoginActivity", "JSON parse error: ${e.message}", e)
                                                showError("登录失败")
                                            }
                                        } else {
                                            showError("登录失败")
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        val message = try {
                                            if (errorBody != null) {
                                                val jsonObject = org.json.JSONObject(errorBody)
                                                jsonObject.optString("message", "账号或密码错误")
                                            } else {
                                                "账号或密码错误"
                                            }
                                        } catch (e: Exception) {
                                            "账号或密码错误"
                                        }
                                        showError(message)
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
                                    val response = NetworkClient.apiService.sendSms(phone = phone)
                                    if (response.isSuccessful) {
                                        val responseBody = response.body()?.string()
                                        if (responseBody?.contains("\"success\":true") == true) {
                                            Toast.makeText(this@LoginActivity, "验证码已发送", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val message = if (responseBody?.contains("message") == true) {
                                                // 提取错误消息
                                                val start = responseBody.indexOf("\"message\":\"") + 11
                                                val end = responseBody.indexOf("\"", start)
                                                if (start < end) responseBody.substring(start, end) else "发送失败"
                                            } else {
                                                "发送失败"
                                            }
                                            showError(message)
                                        }
                                    } else {
                                        showError("发送失败")
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
    onLoginClick: (String, String, Boolean, String, (String) -> Unit) -> Unit,
    onSendSmsClick: (String, (String) -> Unit) -> Unit
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var isSmsMode by remember { mutableStateOf(false) }
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
                onLoginClick(account, password, isSmsMode, smsCode) { msg -> 
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
