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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme

class MobileVerifyPhoneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MobileVerifyPhoneScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBackClick = { finish() },
                        onConfirmClick = {
                            // 逻辑：验证原手机号验证码，保存新手机号
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MobileVerifyPhoneScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    // 假设原手机号是从用户数据中获取的
    val originalPhone = "131****3911"
    var verifyCode by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

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
        Text(text = "原手机号验证修改", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(40.dp))

        // 原手机号展示（打码）
        Text(
            text = "原手机号：$originalPhone",
            fontSize = 16.sp,
            color = Color.Gray,
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

        // 新手机号输入
        OutlinedTextField(
            value = newPhone,
            onValueChange = { newPhone = it },
            label = { Text("请输入新手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 确定按钮
        Button(
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("确定修改", color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(onClick = onBackClick) {
            Text("返回", color = Color.Gray)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MobileVerifyPhonePreview() {
    TravelTheme {
        MobileVerifyPhoneScreen(onBackClick = {}, onConfirmClick = {})
    }
}
