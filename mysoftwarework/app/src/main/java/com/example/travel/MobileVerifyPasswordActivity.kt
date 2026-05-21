package com.example.travel

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
                        onConfirmClick = {
                            // 验证逻辑：校验验证码并保存新密码
                            finish()
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
    onConfirmClick: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var verifyCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
                onClick = { /* 获取验证码逻辑 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
                shape = MaterialTheme.shapes.small
            ) {
                Text("获取验证码", fontSize = 12.sp)
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

        Spacer(modifier = Modifier.height(32.dp))

        // 确认按钮
        Button(
            onClick = onConfirmClick,
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
        MobileVerifyPasswordScreen(onBackClick = {}, onConfirmClick = {})
    }
}
