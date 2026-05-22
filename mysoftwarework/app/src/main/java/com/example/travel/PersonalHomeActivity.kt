package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme

class PersonalHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                PersonalHomeScreen(
                    onNavigate = { page ->
                        when (page) {
                            PageType.HOME -> startActivity(Intent(this@PersonalHomeActivity, HomeActivity::class.java))
                            PageType.RECOMMEND -> startActivity(Intent(this@PersonalHomeActivity, RecommendPostActivity::class.java))
                            PageType.PUBLISH_SCENIC -> startActivity(Intent(this@PersonalHomeActivity, PublishScenicInfoActivity::class.java))
                            PageType.PUBLISH_POST -> startActivity(Intent(this@PersonalHomeActivity, PublishPostActivity::class.java))
                            PageType.PERSONAL -> {}
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PersonalHomeScreen(onNavigate: (PageType) -> Unit) {
    var displayName by remember { mutableStateOf(NetworkClient.userName.orEmpty()) }
    val userTypeLabel = when (NetworkClient.userType) {
        "1" -> "管理员"
        else -> "普通用户"
    }

    LaunchedEffect(Unit) {
        if (displayName.isBlank()) {
            try {
                val res = NetworkClient.apiService.getCurrentUser()
                if (res.success) {
                    res.data?.userName?.let { name ->
                        if (name.isNotBlank()) {
                            displayName = name
                            NetworkClient.userName = name
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(
            currentPage = PageType.PERSONAL,
            onPageChange = onNavigate
        )
        PersonalHomeContent(
            displayName = displayName.ifBlank { "用户" },
            userTypeLabel = userTypeLabel
        )
    }
}

@Composable
fun PersonalHomeContent(displayName: String, userTypeLabel: String) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            context = context,
            onDismiss = { showDeleteDialog = false }
        )
    }

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
                Image(
                    painter = painterResource(R.drawable.ic_default_avatar),
                    contentDescription = "默认头像",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(text = userTypeLabel, fontSize = 12.sp, color = Color.Gray)
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.25f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("个人中心", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                TextButton(
                    onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("个人资料", color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { context.startActivity(Intent(context, MyScenicManagementActivity::class.java)) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("我的景点信息", color = Color.Gray)
                }

                TextButton(
                    onClick = { context.startActivity(Intent(context, MyPostsActivity::class.java)) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("我的推荐帖", color = Color.Gray)
                }

                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(context, MyInteractionListActivity::class.java)
                                .putExtra(MyInteractionListActivity.EXTRA_LIST_TYPE, MyInteractionListActivity.TYPE_LANDSCAPE_FAVORITES)
                        )
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("收藏景点信息", color = Color.Gray)
                }

                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(context, MyInteractionListActivity::class.java)
                                .putExtra(MyInteractionListActivity.EXTRA_LIST_TYPE, MyInteractionListActivity.TYPE_LANDSCAPE_LIKES)
                        )
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("点赞景点信息", color = Color.Gray)
                }

                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(context, MyInteractionListActivity::class.java)
                                .putExtra(MyInteractionListActivity.EXTRA_LIST_TYPE, MyInteractionListActivity.TYPE_POST_FAVORITES)
                        )
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("收藏推荐帖", color = Color.Gray)
                }

                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(context, MyInteractionListActivity::class.java)
                                .putExtra(MyInteractionListActivity.EXTRA_LIST_TYPE, MyInteractionListActivity.TYPE_POST_LIKES)
                        )
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("点赞推荐帖", color = Color.Gray)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = {
                        UserSession.clear(context)
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }) {
                        Text("退出登录")
                    }
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("注销账号", color = Color.White)
                    }
                }
            }
        }
    }
}
