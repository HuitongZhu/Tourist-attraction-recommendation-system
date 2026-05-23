package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

enum class UsernameStatus {
    None,
    Checking,
    Available,
    Exists
}

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegisterScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLoginClick = { finish() },
                        onRegisterClick = { regData, showError ->
                            lifecycleScope.launch {
                                try {
                                    val response = NetworkClient.apiService.register(
                                        userName = regData.username,
                                        account = regData.phone,
                                        password = regData.password,
                                        confirmPassword = regData.confirmPassword
                                    )
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@RegisterActivity, "注册成功", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        showError("注册失败")
                                    }
                                } catch (e: Exception) {
                                    showError("网络连接失败")
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
                                    sendSmsCodeAndShow(
                                        context = this@RegisterActivity,
                                        phone = phone,
                                        type = SmsCodeType.REGISTER,
                                        onError = { msg -> showError(msg) }
                                    )
                                } catch (e: Exception) {
                                    showError("网络异常")
                                }
                            }
                        },
                        onCheckUsername = { username, callback ->
                            lifecycleScope.launch {
                                try {
                                    val response = NetworkClient.apiService.checkUsername(username = username)
                                    if (response.success && response.data != null) {
                                        callback(response.data.available, response.data.message)
                                    } else {
                                        callback(null, response.message ?: "校验失败")
                                    }
                                } catch (e: Exception) {
                                    callback(null, "网络异常")
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
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onRegisterClick: (RegisterRequest, (String) -> Unit) -> Unit,
    onSendSmsClick: (String, (String) -> Unit) -> Unit,
    onCheckUsername: (String, (Boolean?, String?) -> Unit) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var usernameStatus by remember { mutableStateOf<UsernameStatus>(UsernameStatus.None) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(Color(0xFF1A56DB)))
        
        Spacer(modifier = Modifier.height(30.dp))
        Text("账号注册", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A56DB))
        Text("请填写以下信息完成注册", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))

        // 基础必填信息
        SectionTitle("基础账号信息")
        CustomRegisterField(
            value = username, 
            onValueChange = { 
                username = it 
                // 当用户名长度>=2时触发检查
                if (it.length >= 2) {
                    usernameStatus = UsernameStatus.Checking
                    onCheckUsername(it) { available, _ ->
                        usernameStatus = when (available) {
                            true -> UsernameStatus.Available
                            false -> UsernameStatus.Exists
                            else -> UsernameStatus.None
                        }
                    }
                } else {
                    usernameStatus = UsernameStatus.None
                }
            }, 
            label = "用户名 (必填)",
            status = usernameStatus
        )
        CustomRegisterField(value = phone, onValueChange = { phone = it }, label = "手机号 (必填)", keyboardType = KeyboardType.Phone)
        
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = smsCode,
                onValueChange = { smsCode = it },
                label = { Text("验证码") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = { 
                    errorMessage = ""
                    onSendSmsClick(phone) { msg -> 
                        errorMessage = msg 
                    } 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
                modifier = Modifier.height(56.dp)
            ) {
                Text("获取验证码", fontSize = 12.sp)
            }
        }

        CustomRegisterField(value = password, onValueChange = { password = it }, label = "密码 (必填)", isPassword = true)
        CustomRegisterField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "确认密码", isPassword = true)

        Spacer(modifier = Modifier.height(20.dp))
        
        // 个人详细资料 (选填)
        SectionTitle("个人详细资料 (选填)")
        CustomRegisterField(value = realName, onValueChange = { realName = it }, label = "真实姓名")
        CustomRegisterField(value = idNumber, onValueChange = { idNumber = it }, label = "身份证号")
        CustomRegisterField(value = gender, onValueChange = { gender = it }, label = "性别 (男/女)")
        CustomRegisterField(value = birthday, onValueChange = { birthday = it }, label = "生日 (YYYY-MM-DD)")

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                errorMessage = ""
                
                // 1. 检查所有必填项
                if (username.isEmpty() || phone.isEmpty() || password.isEmpty() || smsCode.isEmpty() || confirmPassword.isEmpty()) {
                    errorMessage = "请填写所有必填项"
                    return@Button
                }
                
                // 2. 检查用户名是否可用（如果已经检查过）
                if (usernameStatus == UsernameStatus.Exists) {
                    errorMessage = "该用户名已被注册"
                    return@Button
                }
                
                // 3. 检查密码长度（6位及以上）
                if (password.length < 6) {
                    errorMessage = "密码长度至少需要6位"
                    return@Button
                }
                
                // 4. 检查两次密码是否一致
                if (password != confirmPassword) {
                    errorMessage = "两次输入的密码不一致"
                    return@Button
                }
                
                // 5. 检查手机号格式（必须11位数字）
                if (!phone.matches(Regex("^\\d{11}$"))) {
                    errorMessage = "请输入正确的11位手机号"
                    return@Button
                }
                
                onRegisterClick(
                    RegisterRequest(
                        username = username,
                        phone = phone,
                        password = password,
                        confirmPassword = confirmPassword,
                        smsCode = smsCode,
                        realName = realName.ifEmpty { null },
                        idNumber = idNumber.ifEmpty { null },
                        gender = gender.ifEmpty { null },
                        birthday = birthday.ifEmpty { null }
                    )
                ) { msg -> errorMessage = msg }
            },
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("立即注册", fontSize = 18.sp)
        }

        TextButton(onClick = onLoginClick) {
            Text("已有账号？返回登录", color = Color(0xFF1A56DB))
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
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}

@Composable
fun CustomRegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    status: UsernameStatus = UsernameStatus.None
) {
    Column(modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        // 显示用户名检查状态
        if (status != UsernameStatus.None) {
            Row(modifier = Modifier.padding(top = 4.dp)) {
                when (status) {
                    UsernameStatus.Checking -> {
                        Text(text = "检查中...", color = Color.Gray, fontSize = 12.sp)
                    }
                    UsernameStatus.Available -> {
                        Text(text = "✓ 用户名可用", color = Color.Green, fontSize = 12.sp)
                    }
                    UsernameStatus.Exists -> {
                        Text(text = "✗ 用户名已被使用", color = Color.Red, fontSize = 12.sp)
                    }
                    else -> {}
                }
            }
        }
    }
}
