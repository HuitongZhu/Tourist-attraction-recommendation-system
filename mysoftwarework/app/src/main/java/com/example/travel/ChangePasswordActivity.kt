package com.example.travel

import android.content.Intent
import android.os.Bundle
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
import com.example.travel.ui.theme.TravelTheme

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
                        onConfirmClick = {
                            // 此处添加原密码校验及新密码保存逻辑
                            finish()
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
    onConfirmClick: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
        OutlinedTextField(
            value = oldPassword,
            onValueChange = { oldPassword = it },
            label = { Text("请输入原密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // 新密码
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("设置新密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // 确认新密码
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("再次输入新密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onConfirmClick,
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
