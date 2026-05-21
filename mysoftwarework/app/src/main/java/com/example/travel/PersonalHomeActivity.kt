package com.example.travel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

class PersonalHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopNavBar(
                        currentPage = PageType.PERSONAL,
                        onPageChange = { page ->
                            when (page) {
                                PageType.HOME -> startActivity(Intent(this@PersonalHomeActivity, HomeActivity::class.java))
                                PageType.RECOMMEND -> startActivity(Intent(this@PersonalHomeActivity, RecommendPostActivity::class.java))
                                PageType.PUBLISH_SCENIC -> startActivity(Intent(this@PersonalHomeActivity, PublishScenicInfoActivity::class.java))
                                PageType.PUBLISH_POST -> startActivity(Intent(this@PersonalHomeActivity, PublishPostActivity::class.java))
                                PageType.PERSONAL -> {}
                            }
                        }
                    )
                    PersonalHomeContent()
                }
            }
        }
    }
}

@Composable
fun PersonalHomeContent() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF4A90E2), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "杨", fontSize = 24.sp, color = Color.White)
                }
                Text(text = "杨", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Text(text = "普通用户", fontSize = 12.sp, color = Color.Gray)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("编辑个人资料")
                }
                Button(
                    onClick = { context.startActivity(Intent(context, ChangePhoneActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("修改手机号")
                }
                Button(
                    onClick = { context.startActivity(Intent(context, ChangePasswordActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("修改密码")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.25f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("个人中心", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("个人资料", color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                Text("已发布景点信息", color = Color.Gray)
                Text("我的推荐帖", color = Color.Gray)
                Text("收藏景点信息", color = Color.Gray)
                Text("点赞景点信息", color = Color.Gray)
                // 已在此处删除了“审核景点信息”
            }

            Column(
                modifier = Modifier.fillMaxWidth(0.7f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("个人资料管理", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("基本信息", fontSize = 14.sp, color = Color.Gray)

                InfoItem(label = "用户名", value = "杨")
                InfoItem(label = "真实姓名", value = "杨阳洋")
                InfoItem(label = "用户ID", value = "127832")
                InfoItem(label = "性别", value = "女")
                InfoItem(label = "出生日期", value = "1997年08月09日")
                InfoItem(label = "学历", value = "本科")
                InfoItem(label = "学校/工作单位", value = "南京航空航天大学")
                InfoItem(label = "手机号", value = "31823911")
                InfoItem(label = "电子邮箱", value = "217389@qq.com")

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }) {
                        Text("退出登录")
                    }
                    Button(
                        onClick = { /* 注销账号逻辑 */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("注销账号", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value)
    }
}
