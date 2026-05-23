package com.example.travel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import coil.compose.AsyncImage
import com.example.travel.ui.theme.TravelTheme

class ScenicSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScenicSearchTopNavBar()
                        ScenicSearchContent()
                    }
                }
            }
        }
    }
}

// 顶部导航栏
@Composable
fun ScenicSearchTopNavBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A56DB))
            .topBarSafePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "旅游书",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(text = "首页", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "推荐帖", color = Color(0xFFE0EFFF), fontSize = 16.sp)
            Text(text = "我的", color = Color(0xFFE0EFFF), fontSize = 16.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* 发布景点逻辑 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("发布", fontSize = 12.sp)
            }
            Text(text = "杨", color = Color.White, fontSize = 14.sp)
        }
    }
}

// 景点查询内容区
@Composable
fun ScenicSearchContent() {
    val searchText = remember { mutableStateOf("南京") }
    val sortBy = remember { mutableStateOf("热门优先") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 搜索框
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                placeholder = { Text("搜索景点名称") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            Button(
                onClick = { /* 搜索逻辑 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
            ) {
                Text("搜索")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 筛选标签
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("地点：南京市")
            FilterChip("等级：5A景区")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 排序
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("排序：", fontSize = 14.sp, color = Color.Gray)
            SortOption("热门优先", sortBy.value) { sortBy.value = it }
            SortOption("最新发布", sortBy.value) { sortBy.value = it }
            SortOption("等级排序", sortBy.value) { sortBy.value = it }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ScenicSearchList()
    }
}

@Composable
fun FilterChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE3F2FD), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color(0xFF1A56DB))
    }
}

@Composable
fun SortOption(text: String, selected: String, onSelect: (String) -> Unit) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = if (selected == text) Color(0xFF1A56DB) else Color.Gray,
        fontWeight = if (selected == text) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clickable { onSelect(text) }
            .padding(horizontal = 8.dp)
    )
}

data class ScenicSearch(
    val id: Int,
    val name: String,
    val description: String,
    val location: String,
    val level: String,
    val publishTime: String,
    val imageUrl: String,
    var isFavorite: Boolean = false
)

@Composable
fun ScenicSearchList() {
    val scenics = remember {
        mutableStateOf(listOf(
            ScenicSearch(1, "南京夫子庙", "秦淮风光带核心景区", "南京市秦淮区", "AAAAA级景区", "2025-11-10", "https://img2.baidu.com/it/u=1833634027,3304561858&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500"),
            ScenicSearch(2, "南京中山陵", "国家重点文物保护单位", "南京市玄武区", "AAAAA级景区", "2025-11-05", "https://img2.baidu.com/it/u=3330669165,3799651523&fm=253&fmt=auto&app=138&f=JPEG?w=750&h=500"),
            ScenicSearch(3, "南京明孝陵", "明清皇家陵寝之首", "南京市玄武区", "AAAAA级景区", "2025-11-15", "https://img0.baidu.com/it/u=3156394753,424382676&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500")
        ))
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(scenics.value) { scenic ->
            ScenicSearchItem(
                scenic = scenic,
                onFavoriteClick = {
                    scenics.value = scenics.value.map {
                        if (it.id == scenic.id) it.copy(isFavorite = !it.isFavorite) else it
                    }
                }
            )
        }
    }
}

@Composable
fun ScenicSearchItem(scenic: ScenicSearch, onFavoriteClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            AsyncImage(
                model = scenic.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = scenic.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (scenic.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (scenic.isFavorite) Color.Red else Color.Gray
                        )
                    }
                }

                Text(text = scenic.description, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "地点：${scenic.location}", fontSize = 12.sp, color = Color.Gray)
                Text(text = "级别：${scenic.level}", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            // 跳转详情页逻辑
                            val intent = Intent(context, ScenicDetailActivity::class.java)
                            intent.putExtra("SCENIC_NAME", scenic.name)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
                    ) {
                        Text("查看详情")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScenicSearchPreview() {
    TravelTheme {
        Column {
            ScenicSearchTopNavBar()
            ScenicSearchContent()
        }
    }
}