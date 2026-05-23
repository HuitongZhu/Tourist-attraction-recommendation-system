package com.example.travel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme

class AdminUserInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 顶部区域
                    AdminUserInfoTopBar()
                    // 功能模块区
                    AdminUserInfoContent()
                }
            }
        }
    }
}

// 顶部区域
@Composable
fun AdminUserInfoTopBar() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A56DB))
            .topBarSafePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "旅游书", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("旅游首页", color = Color(0xFFE0EFFF), fontSize = 16.sp, modifier = Modifier.clickable {
                context.startActivity(Intent(context, HomeActivity::class.java))
            })
            Text("景点信息", color = Color(0xFFE0EFFF), fontSize = 16.sp)
            Text("管理员中心", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                context.startActivity(Intent(context, AdminActivity::class.java))
            })
        }

        // 右上角管理员信息
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color(0xFF1A56DB), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Admin", color = Color.White, fontSize = 14.sp)
        }
    }
}

// 功能模块区
@Composable
fun AdminUserInfoContent() {
    val searchText = remember { mutableStateOf("") }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 左侧功能菜单（和管理员功能页面一样）
        Column(
            modifier = Modifier.fillMaxWidth(0.25f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "管理系统功能",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "用户账号管理",
                color = Color(0xFF1A56DB),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "审核景点信息",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, ScenicReviewActivity::class.java))
                }
            )
            Text(
                text = "审核推荐帖",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, PostReviewActivity::class.java))
                }
            )
        }

        // 右侧主内容区
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp)
        ) {
            // 用户信息标题
            Text(
                text = "用户信息",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 搜索栏
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            )

            // 用户信息卡片
            UserInfoCard()
        }
    }
}

// 用户信息卡片
@Composable
fun UserInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 用户基本信息行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用户头像
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF4A90E2), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "杨", fontSize = 18.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 用户信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "杨",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: 127832",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "普通用户",
                        fontSize = 14.sp,
                        color = Color(0xFF1A56DB)
                    )
                }

                // 功能图标（只保留编辑图标）
                IconButton(
                    onClick = { /* 更多操作逻辑 */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "更多操作",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 操作按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* 修改信息逻辑 */ },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("修改信息")
                }
                Button(
                    onClick = { /* 删除用户逻辑 */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除用户")
                }
            }
        }
    }
}

// 预览函数
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminUserInfoPreview() {
    TravelTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminUserInfoTopBar()
            AdminUserInfoContent()
        }
    }
}
