package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travel.ui.theme.TravelTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                var scenicSpots by remember { mutableStateOf<List<LandscapeResponse>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    try {
                        val response = NetworkClient.apiService.getLandscapes(status = "审核通过", size = 6)
                        if (response.success && response.data != null) {
                            scenicSpots = response.data
                            if (scenicSpots.isEmpty()) {
                                Toast.makeText(this@HomeActivity, "暂无景点数据", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@HomeActivity, response.message ?: "获取景点数据失败", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Network error: ${e.message}", e)
                        Toast.makeText(this@HomeActivity, "网络连接失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    TopNavBar(
                        currentPage = PageType.HOME,
                        onPageChange = { page ->
                            when (page) {
                                PageType.RECOMMEND -> startActivity(Intent(this@HomeActivity, RecommendPostActivity::class.java))
                                PageType.PUBLISH_SCENIC -> startActivity(Intent(this@HomeActivity, PublishScenicInfoActivity::class.java))
                                PageType.PUBLISH_POST -> startActivity(Intent(this@HomeActivity, PublishPostActivity::class.java))
                                PageType.PERSONAL -> startActivity(Intent(this@HomeActivity, PersonalHomeActivity::class.java))
                                else -> {}
                            }
                        }
                    )

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        HomeScreen(scenicSpots)
                    }
                }
            }
        }
    }
}

/**
 * 从HTML中解析景点信息
 */
fun parseLandscapesFromHtml(html: String): List<LandscapeResponse> {
    val landscapes = mutableListOf<LandscapeResponse>()
    
    // 打印HTML前500个字符用于调试
    Log.d("HomeActivity", "HTML content preview: ${html.take(500)}...")
    
    // 方法1：使用字符串查找提取数据（更可靠）
    var remainingHtml = html
    val hotLandscapeMarker = "hot-landscape"
    
    if (remainingHtml.contains(hotLandscapeMarker)) {
        // 找到热门景点区域
        val startIndex = remainingHtml.indexOf(hotLandscapeMarker)
        remainingHtml = remainingHtml.substring(startIndex)
        
        // 提取所有景点卡片
        var cardStart = remainingHtml.indexOf("<div")
        while (cardStart != -1) {
            val cardEnd = remainingHtml.indexOf("</div>", cardStart)
            if (cardEnd == -1) break
            
            val cardContent = remainingHtml.substring(cardStart, cardEnd + 6)
            
            // 检查是否是景点卡片
            if (cardContent.contains("card-title") || cardContent.contains("card-info")) {
                // 提取标题
                val titleStart = cardContent.indexOf(">", cardContent.indexOf("card-title")) + 1
                val titleEnd = cardContent.indexOf("<", titleStart)
                val title = if (titleStart > 0 && titleEnd > titleStart) {
                    cardContent.substring(titleStart, titleEnd).trim()
                } else {
                    "未知景点"
                }
                
                // 提取地点
                val infoStart = cardContent.indexOf(">", cardContent.indexOf("card-info")) + 1
                val infoEnd = cardContent.indexOf("<", infoStart)
                val address = if (infoStart > 0 && infoEnd > infoStart) {
                    cardContent.substring(infoStart, infoEnd).trim()
                } else {
                    ""
                }
                
                landscapes.add(
                    LandscapeResponse(
                        id = landscapes.size.toString(),
                        title = title,
                        content = "",
                        address = address,
                        latitude = null,
                        longitude = null,
                        contactPhone = null,
                        openingTime = null,
                        level = null,
                        status = "审核通过",
                        auditRemark = null,
                        publishedAt = null,
                        auditedAt = null,
                        creator = null
                    )
                )
            }
            
            remainingHtml = remainingHtml.substring(cardEnd + 6)
            cardStart = remainingHtml.indexOf("<div")
        }
    }
    
    Log.d("HomeActivity", "Parsed ${landscapes.size} landscapes from HTML")
    
    return landscapes
}

@Composable
fun HomeScreen(spots: List<LandscapeResponse>) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(spots) { spot ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, ScenicDetailActivity::class.java)
                        intent.putExtra("landscapeId", spot.id)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    AsyncImage(
                        model = "https://via.placeholder.com/400x200.png?text=${spot.title}",
                        contentDescription = spot.title,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = spot.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "地点：${spot.address}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = spot.content, fontSize = 14.sp, maxLines = 2)
                    }
                }
            }
        }
    }
}
