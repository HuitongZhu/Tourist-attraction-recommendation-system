package com.example.travel

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

enum class VerifyMethod {
    PASSWORD, SMS
}

@Composable
fun DeleteAccountDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var verifyMethod by remember { mutableStateOf(VerifyMethod.PASSWORD) }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: 选择验证方式, 2: 验证, 3: 确认注销
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var canProceed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            when (step) {
                1 -> Text("账号注销", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                2 -> Text("身份验证", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                3 -> Text("确认注销", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> {
                        Text("请选择身份验证方式", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = verifyMethod == VerifyMethod.PASSWORD,
                                onClick = { verifyMethod = VerifyMethod.PASSWORD }
                            )
                            Text("密码验证", fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = verifyMethod == VerifyMethod.SMS,
                                onClick = { verifyMethod = VerifyMethod.SMS }
                            )
                            Text("手机号验证", fontSize = 14.sp)
                        }
                    }
                    2 -> {
                        if (verifyMethod == VerifyMethod.PASSWORD) {
                            Text("请输入当前密码进行验证", color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            PasswordTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "密码",
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("请输入手机号并获取验证码", color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("手机号") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row {
                                OutlinedTextField(
                                    value = smsCode,
                                    onValueChange = { smsCode = it },
                                    label = { Text("验证码") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (phone.length != 11) {
                                            errorMessage = "请输入正确的11位手机号"
                                        } else {
                                            isLoading = true
                                            coroutineScope.launch {
                                                try {
                                                    sendSmsCodeAndShow(
                                                        context = context,
                                                        phone = phone,
                                                        type = SmsCodeType.DELETE,
                                                        onMessage = { msg -> errorMessage = msg }
                                                    )
                                                } catch (e: Exception) {
                                                    errorMessage = "网络异常"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text("发送", fontSize = 12.sp)
                                }
                            }
                        }
                        if (errorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                    3 -> {
                        Text("确定要注销账号吗？", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("注销后所有数据将被删除，且无法恢复！", color = Color.Red, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (step) {
                        1 -> {
                            step = 2
                        }
                        2 -> {
                            errorMessage = ""
                            if (verifyMethod == VerifyMethod.PASSWORD) {
                                if (password.isEmpty()) {
                                    errorMessage = "请输入密码"
                                    return@Button
                                }
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val response = NetworkClient.apiService.verifyPassword(password)
                                        if (response.success) {
                                            step = 3
                                            canProceed = true
                                        } else {
                                            errorMessage = response.message ?: "密码错误"
                                        }
                                    } catch (e: retrofit2.HttpException) {
                                        when (e.code()) {
                                            404 -> errorMessage = "服务暂未开放此功能"
                                            401 -> errorMessage = "密码错误"
                                            else -> errorMessage = "验证失败: ${e.code()}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "网络异常"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                if (phone.isEmpty() || smsCode.isEmpty()) {
                                    errorMessage = "请输入手机号和验证码"
                                    return@Button
                                }
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        // 使用正确的注销验证码验证接口
                                        val response = NetworkClient.apiService.verifyDeleteSmsCode(phone, smsCode)
                                        if (response.success) {
                                            step = 3
                                            canProceed = true
                                        } else {
                                            errorMessage = response.message ?: "验证码错误"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "网络异常"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                        3 -> {
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val response = NetworkClient.apiService.deleteAccount()
                                    if (response.success) {
                                        UserSession.clear(context)
                                        Toast.makeText(context, "账号已注销", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "注销失败", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = if (step == 3) Color.Red else Color(0xFF1A56DB))
            ) {
                Text(
                    when (step) {
                        1 -> "下一步"
                        2 -> "验证"
                        3 -> "确认注销"
                        else -> "确认"
                    },
                    color = if (step == 3) Color.White else Color.White
                )
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    if (step > 1) {
                        step--
                    } else {
                        onDismiss()
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(if (step > 1) "返回" else "取消", color = Color.White)
            }
        }
    )
}