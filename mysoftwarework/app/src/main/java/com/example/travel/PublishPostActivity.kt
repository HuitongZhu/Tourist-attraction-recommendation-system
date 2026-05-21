package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

class PublishPostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    TopNavBar(
                        currentPage = PageType.PUBLISH_POST,
                        onPageChange = { page ->
                            when (page) {
                                PageType.HOME -> startActivity(Intent(this@PublishPostActivity, HomeActivity::class.java))
                                PageType.RECOMMEND -> startActivity(Intent(this@PublishPostActivity, RecommendPostActivity::class.java))
                                PageType.PUBLISH_SCENIC -> startActivity(Intent(this@PublishPostActivity, PublishScenicInfoActivity::class.java))
                                PageType.PERSONAL -> startActivity(Intent(this@PublishPostActivity, PersonalHomeActivity::class.java))
                                else -> {}
                            }
                        }
                    )
                    PublishPostContent()
                }
            }
        }
    }
}

@Composable
fun PublishPostContent() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var title by remember { mutableStateOf("") }
    var linkedScenic by remember { mutableStateOf("") }
    var currentTag by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf("攻略", "美食") }
    var content by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(text = "发布推荐帖", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        Surface(
            color = Color(0xFFFFF9C4),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "提示：推荐帖需关联具体景点，发布后1-2个工作日审核，通过后在“推荐”板块展示",
                fontSize = 13.sp,
                color = Color(0xFF8B8000),
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 标题
        Text("推荐帖标题", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("请输入推荐帖标题（如：南京游玩指南）", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 关联景点
        Text("关联景点", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = linkedScenic,
            onValueChange = { linkedScenic = it },
            placeholder = { Text("请选择/输入关联景点（如：北京故宫）", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 推荐标签
        Text("推荐标签", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = currentTag,
                onValueChange = { currentTag = it },
                placeholder = { Text("输入标签后点击添加", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (currentTag.isNotBlank()) {
                        tags.add(currentTag)
                        currentTag = ""
                    }
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                modifier = Modifier.height(56.dp)
            ) {
                Text("添加")
            }
        }
        
        // 展示标签
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                Surface(
                    color = Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { tags.remove(tag) }
                ) {
                    Text(tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 推荐图片
        Text("推荐图片", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        Box(
            modifier = Modifier.size(100.dp).background(Color(0xFFF5F5F5)).border(1.dp, Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
        }
        Text("支持JPG、PNG格式，建议16:9", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // 推荐内容
        Text("推荐内容", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("请详细描述推荐内容（如游玩攻略、景点亮点等）", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(context, "请填写必填项", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                activity?.lifecycleScope?.launch {
                    try {
                        val request = PostRequest(
                            title = title,
                            content = content,
                            tag = tags.joinToString(","),
                            landscapeId = null // 实际应用中应关联具体ID
                        )
                        val response = NetworkClient.apiService.createPost(request)
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("发布", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
