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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme

class ChangePhoneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChangePhoneScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBackClick = { finish() },
                        onMobileVerifyClick = {
                            startActivity(Intent(this, MobileVerifyPhoneActivity::class.java))
                        },
                        onConfirmClick = {
                            // 逻辑1：验证密码并更新手机号
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChangePhoneScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onMobileVerifyClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 顶部蓝色装饰条
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(Color(0xFF1A56DB)))
        
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "旅游书", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A56DB))
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "修改手机号", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(40.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("请输入登录密码验证身份") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = newPhone,
            onValueChange = { newPhone = it },
            label = { Text("请输入新手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
        ) {
            Text(text = "确定修改", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(onClick = onMobileVerifyClick) {
            Text(text = "原手机号验证修改 >", fontSize = 14.sp, color = Color(0xFF1A56DB))
        }

        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(onClick = onBackClick, modifier = Modifier.padding(bottom = 32.dp)) {
            Text(text = "返回", color = Color.Gray)
        }
    }
}
