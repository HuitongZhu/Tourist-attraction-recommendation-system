package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class ViewProfileActivity : ComponentActivity() {
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
                                PageType.HOME -> startActivity(Intent(this@ViewProfileActivity, HomeActivity::class.java))
                                PageType.RECOMMEND -> startActivity(Intent(this@ViewProfileActivity, RecommendPostActivity::class.java))
                                PageType.PUBLISH_SCENIC -> startActivity(Intent(this@ViewProfileActivity, PublishScenicInfoActivity::class.java))
                                PageType.PUBLISH_POST -> startActivity(Intent(this@ViewProfileActivity, PublishPostActivity::class.java))
                                PageType.PERSONAL -> finish()
                                else -> {}
                            }
                        }
                    )
                    ViewProfileContent(
                        onEdit = {
                            startActivity(Intent(this@ViewProfileActivity, EditProfileActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ViewProfileContent(onEdit: () -> Unit) {
    var user by remember { mutableStateOf<UserResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    suspend fun reload(showFullLoading: Boolean = true) {
        if (showFullLoading) loading = true
        try {
            val data = loadUserProfile()
            if (data != null) {
                user = data
            } else {
                Toast.makeText(context, "加载失败，请确认已登录并重启后端", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { scope.launch { isRefreshing = true; reload(showFullLoading = false) } }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("个人资料", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            if (loading && user == null) {
                CircularProgressIndicator()
            } else {
                val u = user
                ProfileReadOnlyRow("用户名", u?.userName)
                ProfileReadOnlyRow("真实姓名", u?.realName)
                ProfileReadOnlyRow("性别", u?.gender)
                ProfileReadOnlyRow("生日", u?.birthday)
                ProfileReadOnlyRow("手机号", u?.phoneNumber)
                ProfileReadOnlyRow("身份证号", u?.idNumber)

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
                ) {
                    Text("编辑个人资料")
                }
            }
        }
    }
}

@Composable
private fun ProfileReadOnlyRow(label: String, value: String?) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "未填写",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Divider(modifier = Modifier.padding(top = 8.dp), color = Color(0xFFEEEEEE))
    }
}
