package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                HomePage()
            }
        }
    }
}

@Composable
private fun HomePage() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()

    var scenicSpots by remember { mutableStateOf<List<LandscapeResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    suspend fun loadData(showFullLoading: Boolean = false) {
        if (showFullLoading) isLoading = true
        try {
            scenicSpots = loadHomeLandscapes(searchQuery.trim())
        } catch (e: Exception) {
            Log.e("HomeActivity", "Load landscapes failed: ${e.message}", e)
            Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            scenicSpots = emptyList()
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(searchQuery) {
        val keyword = searchQuery.trim()
        if (keyword.isNotEmpty()) delay(400)
        loadData(showFullLoading = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(
            currentPage = PageType.HOME,
            onPageChange = { page ->
                when (page) {
                    PageType.RECOMMEND -> activity?.startActivity(Intent(context, RecommendPostActivity::class.java))
                    PageType.PUBLISH_SCENIC -> activity?.startActivity(Intent(context, PublishScenicInfoActivity::class.java))
                    PageType.PUBLISH_POST -> activity?.startActivity(Intent(context, PublishPostActivity::class.java))
                    PageType.PERSONAL -> activity?.startActivity(Intent(context, PersonalHomeActivity::class.java))
                    else -> {}
                }
            }
        )

        HomeSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            resultCount = scenicSpots.size,
            isLoading = isLoading && !isRefreshing
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    loadData(showFullLoading = false)
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            when {
                isLoading && scenicSpots.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                scenicSpots.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isBlank()) "暂无已审核景点" else "未找到匹配的景点",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
                else -> {
                    HomeScreen(scenicSpots)
                }
            }
        }
    }
}

@Composable
private fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    resultCount: Int,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索景点名称、地址或介绍…") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A56DB),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
        if (!isLoading) {
            Text(
                text = if (query.isBlank()) "共 $resultCount 个景点" else "找到 $resultCount 个相关景点",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

/** 加载首页景点：优先新接口（全部/模糊搜索），失败则回退 */
private suspend fun loadHomeLandscapes(keyword: String): List<LandscapeResponse> {
    val api = NetworkClient.apiService
    val kw = keyword.takeIf { it.isNotBlank() }

    try {
        val res = api.getHomeLandscapeList(kw)
        if (res.success && res.data != null) {
            return res.data.map { it.toLandscapeResponse() }
        }
    } catch (e: HttpException) {
        if (e.code() != 404) throw e
        Log.w("HomeActivity", "home-list 404, fallback")
    }

    try {
        val approved = api.getApprovedLandscapes()
        if (approved.success && approved.data != null) {
            val list = approved.data.map { it.toLandscapeResponse() }
            return if (kw == null) list else fuzzyFilterLandscapes(list, kw)
        }
    } catch (e: HttpException) {
        if (e.code() != 404) throw e
    }

    val legacy = api.getLandscapes(status = "审核通过", size = 500)
    if (legacy.success && legacy.data != null) {
        val list = legacy.data
        return if (kw == null) list else fuzzyFilterLandscapes(list, kw)
    }
    return emptyList()
}

/** 客户端模糊匹配（标题、地址、介绍） */
private fun fuzzyFilterLandscapes(list: List<LandscapeResponse>, keyword: String): List<LandscapeResponse> {
    val q = keyword.trim().lowercase()
    if (q.isEmpty()) return list
    return list.filter { spot ->
        spot.title.lowercase().contains(q) ||
            spot.address.lowercase().contains(q) ||
            spot.content.lowercase().contains(q) ||
            (spot.level?.lowercase()?.contains(q) == true)
    }
}

/** 瀑布流卡片图片高度（仿小红书错落排版） */
private fun homeCardImageHeight(spotId: String, index: Int): Dp {
    val heights = listOf(168.dp, 200.dp, 184.dp, 216.dp, 176.dp, 208.dp)
    val key = (spotId.hashCode() and 0x7FFFFFFF) + index
    return heights[key % heights.size]
}

@Composable
fun HomeScreen(spots: List<LandscapeResponse>) {
    val context = LocalContext.current
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7)),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalItemSpacing = 6.dp
    ) {
        items(spots.size, key = { spots[it].id }) { index ->
            val spot = spots[index]
            HomeScenicFeedCard(
                spot = spot,
                imageHeight = homeCardImageHeight(spot.id, index),
                onClick = {
                    val intent = Intent(context, ScenicDetailActivity::class.java)
                    intent.putExtra("landscapeId", spot.id)
                    context.startActivity(intent)
                }
            )
        }
    }
}

/** 小红书风格：双列瀑布流、图片铺满裁切、标题+地点紧凑展示 */
@Composable
fun HomeScenicFeedCard(
    spot: LandscapeResponse,
    imageHeight: Dp,
    onClick: () -> Unit
) {
    val imageModel = NetworkClient.mediaUrl(spot.imagePath)
        ?: "https://via.placeholder.com/400x520.png?text=${spot.title}"
    val cardShape = RoundedCornerShape(10.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = cardShape,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column {
            AsyncImage(
                model = imageModel,
                contentDescription = spot.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Text(
                    text = spot.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF222222),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                if (spot.address.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF999999)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = spot.address,
                            fontSize = 11.sp,
                            color = Color(0xFF999999),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (!spot.level.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = spot.level,
                        fontSize = 10.sp,
                        color = Color(0xFF1A56DB),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
