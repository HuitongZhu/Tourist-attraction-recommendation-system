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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ForgotPasswordScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBackClick = { finish() },
                        onResetSuccess = {
                            Toast.makeText(this, "密码重置成功", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

enum class ForgotPasswordStep {
    INPUT_ACCOUNT,    // 输入账号
    VERIFY_ACCOUNT,   // 验证账号（发送验证码和输入验证码）
    INPUT_PASSWORD,   // 输入新密码
    CONFIRM_PASSWORD  // 确认新密码
}

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.INPUT_ACCOUNT) }
    var account by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
        Text(text = "忘记密码", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(30.dp))

        // 错误消息显示
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(15.dp))
        }

        when (currentStep) {
            ForgotPasswordStep.INPUT_ACCOUNT -> {
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("手机号") },
                    placeholder = { Text("请输入手机号") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        errorMessage = ""
                        if (account.isEmpty()) {
                            errorMessage = "请输入手机号"
                            return@Button
                        }
                        if (!account.matches(Regex("^\\d{11}$"))) {
                            errorMessage = "请输入正确的11位手机号"
                            return@Button
                        }
                        currentStep = ForgotPasswordStep.VERIFY_ACCOUNT
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(text = "下一步", fontSize = 16.sp)
                }
            }

            ForgotPasswordStep.VERIFY_ACCOUNT -> {
                Text(text = "已向手机号 $account 发送验证码", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(15.dp))
                Row(modifier = Modifier.fillMaxWidth(0.8f)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("验证码") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            errorMessage = ""
                            if (!account.matches(Regex("^\\d{11}$"))) {
                                errorMessage = "请输入正确的11位手机号"
                                return@Button
                            }
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val response = NetworkClient.apiService.sendSms(account)
                                    if (response.isSuccessful) {
                                        val body = response.body()?.string()
                                        if (body?.contains("\"success\":true") == true) {
                                            Toast.makeText(context, "验证码已发送", Toast.LENGTH_SHORT).show()
                                        } else {
                                            errorMessage = "发送失败"
                                        }
                                    } else {
                                        errorMessage = "发送失败"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "网络异常"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.width(120.dp),
                        enabled = !isLoading
                    ) {
                        Text(text = "获取验证码", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        errorMessage = ""
                        if (code.isEmpty()) {
                            errorMessage = "请输入验证码"
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val response = NetworkClient.apiService.verifyRegisterCode(account, code)
                                if (response.isSuccessful) {
                                    val body = response.body()?.string()
                                    if (body?.contains("\"success\":true") == true) {
                                        currentStep = ForgotPasswordStep.INPUT_PASSWORD
                                    } else {
                                        errorMessage = "验证码错误"
                                    }
                                } else {
                                    errorMessage = "验证码错误"
                                }
                            } catch (e: Exception) {
                                errorMessage = "网络异常"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    enabled = !isLoading
                ) {
                    Text(text = "验证", fontSize = 16.sp)
                }
            }

            ForgotPasswordStep.INPUT_PASSWORD -> {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("新密码") },
                    placeholder = { Text("请输入新密码（至少6位）") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        errorMessage = ""
                        if (newPassword.length < 6) {
                            errorMessage = "密码长度至少需要6位"
                            return@Button
                        }
                        if (!newPassword.matches(Regex("^[a-zA-Z0-9]+$"))) {
                            errorMessage = "密码只能包含字母和数字"
                            return@Button
                        }
                        currentStep = ForgotPasswordStep.CONFIRM_PASSWORD
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(text = "下一步", fontSize = 16.sp)
                }
            }

            ForgotPasswordStep.CONFIRM_PASSWORD -> {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("确认新密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        errorMessage = ""
                        if (confirmPassword.isEmpty()) {
                            errorMessage = "请再次输入密码"
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            errorMessage = "两次输入的密码不一致"
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val response = NetworkClient.apiService.resetPasswordBySms(
                                    phone = account,
                                    code = code,
                                    newPassword = newPassword
                                )
                                if (response.success) {
                                    onResetSuccess()
                                } else {
                                    errorMessage = response.message ?: "重置失败"
                                }
                            } catch (e: Exception) {
                                errorMessage = "网络异常"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    enabled = !isLoading
                ) {
                    Text(text = "确认重置", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        // 返回按钮（除了第一步）
        if (currentStep != ForgotPasswordStep.INPUT_ACCOUNT) {
            TextButton(onClick = {
                when (currentStep) {
                    ForgotPasswordStep.VERIFY_ACCOUNT -> {
                        currentStep = ForgotPasswordStep.INPUT_ACCOUNT
                        code = ""
                    }
                    ForgotPasswordStep.INPUT_PASSWORD -> {
                        currentStep = ForgotPasswordStep.VERIFY_ACCOUNT
                        newPassword = ""
                    }
                    ForgotPasswordStep.CONFIRM_PASSWORD -> {
                        currentStep = ForgotPasswordStep.INPUT_PASSWORD
                        confirmPassword = ""
                    }
                    else -> {}
                }
                errorMessage = ""
            }) {
                Text(text = "上一步", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        // 返回登录
        TextButton(onClick = onBackClick) {
            Text(text = "返回登录", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    TravelTheme {
        ForgotPasswordScreen(
            onBackClick = {},
            onResetSuccess = {}
        )
    }
}
