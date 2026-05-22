package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishPostContent() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var title by remember { mutableStateOf("") }
    var selectedLandscapeId by remember { mutableStateOf<String?>(null) }
    var selectedLandscapeTitle by remember { mutableStateOf("") }
    var landscapeMenuExpanded by remember { mutableStateOf(false) }
    /** 与首页相同数据源：已审核通过的景点 */
    var landscapeOptions by remember { mutableStateOf<List<LandscapeResponse>>(emptyList()) }
    var landscapesLoading by remember { mutableStateOf(true) }

    var currentTag by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>() }
    var content by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        landscapesLoading = true
        try {
            // 与首页 HomeActivity 使用同一接口，保证下拉与首页展示一致
            val res = NetworkClient.apiService.getLandscapes(status = "审核通过", size = 200)
            if (res.success && res.data != null) {
                landscapeOptions = res.data
            } else {
                // 若新接口已部署，再尝试拉取全部已审核景点
                val extra = NetworkClient.apiService.getApprovedLandscapes()
                if (extra.success && extra.data != null) {
                    landscapeOptions = extra.data.map { it.toLandscapeResponse() }
                }
            }
        } catch (_: Exception) {
        } finally {
            landscapesLoading = false
        }
    }

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
                text = "提示：推荐帖需关联已审核通过的景点，发布后等待管理员审核",
                fontSize = 13.sp,
                color = Color(0xFF8B8000),
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("推荐帖标题", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("请输入推荐帖标题（如：南京游玩指南）", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("关联景点", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        if (landscapesLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else if (landscapeOptions.isEmpty()) {
            Text("暂无景点，请先在首页确认有已审核通过的景点", fontSize = 13.sp, color = Color.Gray)
        } else {
            ExposedDropdownMenuBox(
                expanded = landscapeMenuExpanded,
                onExpandedChange = { landscapeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedLandscapeTitle,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("请选择关联景点", fontSize = 14.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = landscapeMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                )
                ExposedDropdownMenu(
                    expanded = landscapeMenuExpanded,
                    onDismissRequest = { landscapeMenuExpanded = false }
                ) {
                    landscapeOptions.forEach { landscape ->
                        DropdownMenuItem(
                            text = { Text(landscape.title) },
                            onClick = {
                                selectedLandscapeId = landscape.id
                                selectedLandscapeTitle = landscape.title
                                landscapeMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("推荐标签", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = currentTag,
                onValueChange = { currentTag = it },
                placeholder = { Text("输入标签文字后点击添加", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val t = currentTag.trim()
                    if (t.isEmpty()) return@Button
                    if (tags.contains(t)) {
                        Toast.makeText(context, "标签已存在", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    tags.add(t)
                    currentTag = ""
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                modifier = Modifier.height(56.dp)
            ) {
                Text("添加")
            }
        }

        if (tags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags.toList(), key = { it }) { tag ->
                    AssistChip(
                        onClick = { tags.remove(tag) },
                        label = { Text(tag, fontSize = 12.sp) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                if (title.isBlank() || content.isBlank()) {
                    Toast.makeText(context, "请填写标题和推荐内容", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (selectedLandscapeId.isNullOrBlank()) {
                    Toast.makeText(context, "请选择关联景点", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!UserSession.isLoggedIn()) {
                    Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                activity?.lifecycleScope?.launch {
                    try {
                        val request = PostRequest(
                            title = title.trim(),
                            content = content.trim(),
                            landscapeId = selectedLandscapeId,
                            tag = tags.joinToString(",").ifBlank { null }
                        )
                        val response = NetworkClient.apiService.publishPost(request)
                        if (response.success) {
                            Toast.makeText(context, "发布成功，等待管理员审核", Toast.LENGTH_SHORT).show()
                            activity.finish()
                        } else {
                            Toast.makeText(context, response.message ?: "发布失败", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: retrofit2.HttpException) {
                        val msg = when (e.code()) {
                            405, 404 -> "后端未更新：请 Rebuild 并重启 TravelWebApplication"
                            else -> "请求失败 HTTP ${e.code()}"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
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
