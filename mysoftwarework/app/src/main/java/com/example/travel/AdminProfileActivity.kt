package com.example.travel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme

class AdminProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    // 1. 顶部导航栏 (只显示文字，隐藏头像)
                    AdminTopNavBar(showAvatar = false)

                    Row(modifier = Modifier.fillMaxSize()) {
                        // 2. 左侧侧边栏
                        AdminSidebar(selectedModule = "PROFILE")

                        // 3. 右侧主内容区
                        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                            AdminProfileContent(onBack = { finish() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProfileContent(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 顶部返回按钮
        TextButton(
            onClick = onBack,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("← 返回", fontSize = 18.sp, color = Color(0xFF1A56DB), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 模仿截图排版：头像区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFF4A90E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "管", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Column {
                Text(text = "管理员", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "超级权限已启用", fontSize = 16.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 详细资料区域 (双列模仿截图)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左小列：标识
            Column(
                modifier = Modifier.fillMaxWidth(0.3f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("个人中心", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("管理员详情", color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // 右大列：核心信息
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text("基础身份信息", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                
                // 使用 Divider 替代当前版本不支持的 HorizontalDivider
                Divider(color = Color(0xFFF3F4F6))

                // 仅显示要求的两项
                AdminSimpleInfoItem(label = "管理员ID", value = "admin_127832")
                AdminSimpleInfoItem(label = "管理员身份", value = "超级管理员")

                Spacer(modifier = Modifier.height(32.dp))

                // 退出登录按钮
                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("退出登录", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun AdminSimpleInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF757575), fontSize = 18.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
