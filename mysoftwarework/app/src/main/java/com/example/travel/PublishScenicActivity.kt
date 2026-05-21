package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class PublishScenicInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    TopNavBar(
                        currentPage = PageType.PUBLISH_SCENIC,
                        onPageChange = { page ->
                            when (page) {
                                PageType.HOME -> startActivity(Intent(this@PublishScenicInfoActivity, HomeActivity::class.java))
                                PageType.RECOMMEND -> startActivity(Intent(this@PublishScenicInfoActivity, RecommendPostActivity::class.java))
                                PageType.PERSONAL -> startActivity(Intent(this@PublishScenicInfoActivity, PersonalHomeActivity::class.java))
                                else -> {}
                            }
                        }
                    )
                    PublishScenicInfoContent()
                }
            }
        }
    }
}

@Composable
fun PublishScenicInfoContent() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var scenicName by remember { mutableStateOf("") }
    var scenicLocation by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("") }
    var scenicLevel by remember { mutableStateOf("") }
    var scenicDetails by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val scrollState = rememberScrollState()

    // 自动获取经纬度
    LaunchedEffect(scenicName) {
        if (scenicName.length > 2) {
            try {
                val response = NetworkClient.apiService.geocode(scenicName)
                if (response.success && response.data != null) {
                    latitude = response.data.latitude
                    longitude = response.data.longitude
                }
            } catch (e: Exception) {
                // 静默失败
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Text(text = "发布景点信息", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        Surface(
            color = Color(0xFFFFF9C4),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "提示：发布的景点信息需经过管理员审核，审核通过后展示",
                fontSize = 14.sp,
                color = Color(0xFF8B8000),
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            FormField("景点名称", scenicName, { scenicName = it }, "请输入景点名称")

            // 图片占位
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                    Text("点击上传图片", fontSize = 14.sp, color = Color.Gray)
                }
            }

            FormField("景点地点", scenicLocation, { scenicLocation = it }, "请输入详细地点")
            
            // 显示获取到的经纬度（只读展示）
            if (latitude != null && longitude != null) {
                Text(
                    text = "已自动获取位置: 纬度 ${String.format("%.4f", latitude)}, 经度 ${String.format("%.4f", longitude)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            FormField("联系方式", contact, { contact = it }, "请输入联系电话")
            FormField("开放时间", openTime, { openTime = it }, "请输入开放时间")
            FormField("景点等级", scenicLevel, { scenicLevel = it }, "请输入景点等级")

            Column {
                Text("景点详情", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = scenicDetails,
                    onValueChange = { scenicDetails = it },
                    placeholder = { Text("请输入景点详情") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (scenicName.isEmpty() || scenicLocation.isEmpty()) {
                        Toast.makeText(context, "请填写必填项", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    activity?.lifecycleScope?.launch {
                        try {
                            val request = LandscapeRequest(
                                title = scenicName,
                                content = scenicDetails,
                                address = scenicLocation,
                                latitude = latitude,
                                longitude = longitude,
                                contactPhone = contact,
                                openingTime = openTime,
                                level = scenicLevel
                            )
                            val response = NetworkClient.apiService.createLandscape(request)
                            if (response.success) {
                                Toast.makeText(context, "发布成功，等待审核", Toast.LENGTH_SHORT).show()
                                activity.finish()
                            } else {
                                Toast.makeText(context, "失败: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("发布", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


