package com.example.travel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme

class MyScenicManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 顶部导航
                    MyScenicTopNavBar()
                    // 我的景点管理内容
                    MyScenicManagementContent()
                }
            }
        }
    }
}

// 顶部导航
@Composable
fun MyScenicTopNavBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A56DB))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：系统名称
        Text(
            text = "",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // 中间：导航栏
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "首页",
                color = Color(0xFFE0EFFF),
                fontSize = 16.sp
            )
            Text(
                text = "推荐帖",
                color = Color(0xFFE0EFFF),
                fontSize = 16.sp
            )
            Text(
                text = "我的",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 右侧：操作按钮和用户标识
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* 发布景点逻辑 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("发布景点", fontSize = 14.sp)
            }
            Text(
                text = "杨",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

// 我的景点管理内容
@Composable
fun MyScenicManagementContent() {
    val sortBy = remember { mutableStateOf("创建日期（最新在前）") }
    val filterStatus = remember { mutableStateOf("全部状态") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // 页面标题
        Text(
            text = "我的景点管理",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 筛选区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 排序方式
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "排序方式",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = sortBy.value,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    readOnly = true
                )
            }

            // 审核状态
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "审核状态",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = filterStatus.value,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    readOnly = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 景点列表
        ScenicManagementList()
    }
}

// 景点数据类
data class ScenicManagement(
    val name: String,
    val description: String,
    val location: String,
    val level: String,
    val createTime: String,
    val status: String,
    val statusColor: Color
)

// 景点列表
@Composable
fun ScenicManagementList() {
    val scenics = listOf(
        ScenicManagement(
            "南京夫子庙",
            "秦淮风光带核心景区",
            "南京市秦淮区",
            "AAAAA级景区",
            "2025-11-10",
            "审核通过",
            Color(0xFF4CAF50) // 绿色
        ),
        ScenicManagement(
            "黄山风景区",
            "冬季雪景攻略",
            "黄山市黄山区",
            "AAAAA级景区",
            "2025-11-20",
            "待审核",
            Color(0xFFFF9800) // 黄色
        ),
        ScenicManagement(
            "苏州拙政园",
            "古典园林代表",
            "苏州市姑苏区",
            "AAAAA级景区",
            "2025-11-05",
            "审核未通过",
            Color(0xFFE91E63) // 粉色
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(scenics) { scenic ->
            ScenicManagementItem(scenic = scenic)
        }
    }
}

// 景点管理项组件
@Composable
fun ScenicManagementItem(scenic: ScenicManagement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 景点名称和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = scenic.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scenic.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // 状态标签
                Box(
                    modifier = Modifier
                        .background(scenic.statusColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = scenic.status,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 景点信息
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "地点：${scenic.location}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "等级：${scenic.level}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "创建时间：${scenic.createTime}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 操作按钮
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 查看详情按钮
                OutlinedButton(
                    onClick = { /* 查看详情逻辑 */ },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("查看详情")
                }

                // 编辑/重新编辑按钮
                Button(
                    onClick = { /* 编辑逻辑 */ },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
                ) {
                    Text(
                        text = if (scenic.status == "审核未通过") "重新编辑" else "编辑",
                        fontSize = 14.sp
                    )
                }

                // 删除按钮
                Button(
                    onClick = { /* 删除逻辑 */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("删除", fontSize = 14.sp)
                }
            }
        }
    }
}

// 预览函数
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyScenicManagementPreview() {
    TravelTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            MyScenicTopNavBar()
            MyScenicManagementContent()
        }
    }
}