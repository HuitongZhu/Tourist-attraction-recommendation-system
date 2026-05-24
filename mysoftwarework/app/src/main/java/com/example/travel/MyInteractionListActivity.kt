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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class MyInteractionListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listType = intent.getStringExtra(EXTRA_LIST_TYPE) ?: TYPE_LANDSCAPE_LIKES
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                MyInteractionListScreen(listType = listType, onBack = { finish() })
            }
        }
    }

    companion object {
        const val EXTRA_LIST_TYPE = "list_type"
        const val TYPE_LANDSCAPE_LIKES = "landscape_likes"
        const val TYPE_LANDSCAPE_FAVORITES = "landscape_favorites"
        const val TYPE_POST_LIKES = "post_likes"
        const val TYPE_POST_FAVORITES = "post_favorites"

        fun titleFor(type: String): String = when (type) {
            TYPE_LANDSCAPE_LIKES -> "点赞的景点"
            TYPE_LANDSCAPE_FAVORITES -> "收藏的景点"
            TYPE_POST_LIKES -> "点赞的推荐帖"
            TYPE_POST_FAVORITES -> "收藏的推荐帖"
            else -> "我的互动"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInteractionListScreen(listType: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var landscapes by remember { mutableStateOf<List<LandscapeBackendResponse>>(emptyList()) }
    var posts by remember { mutableStateOf<List<PostBackendResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val isLandscapeList = listType == MyInteractionListActivity.TYPE_LANDSCAPE_LIKES ||
            listType == MyInteractionListActivity.TYPE_LANDSCAPE_FAVORITES

    suspend fun reload(showFullLoading: Boolean = true) {
        if (showFullLoading) loading = true
        if (!UserSession.isLoggedIn()) {
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
            loading = false
            isRefreshing = false
            return
        }
        try {
            when (listType) {
                MyInteractionListActivity.TYPE_LANDSCAPE_LIKES -> {
                    val res = NetworkClient.apiService.getMyLandscapeLikes()
                    if (res.success) landscapes = res.data ?: emptyList()
                }
                MyInteractionListActivity.TYPE_LANDSCAPE_FAVORITES -> {
                    val res = NetworkClient.apiService.getMyLandscapeFavorites()
                    if (res.success) landscapes = res.data ?: emptyList()
                }
                MyInteractionListActivity.TYPE_POST_LIKES -> {
                    val res = NetworkClient.apiService.getMyPostLikes()
                    if (res.success) posts = res.data ?: emptyList()
                }
                MyInteractionListActivity.TYPE_POST_FAVORITES -> {
                    val res = NetworkClient.apiService.getMyPostFavorites()
                    if (res.success) posts = res.data ?: emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("MyInteractionList", "load failed: ${e.message}", e)
            Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(listType) {
        reload()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
                title = { Text(MyInteractionListActivity.titleFor(listType)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { scope.launch { isRefreshing = true; reload(showFullLoading = false) } },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading && landscapes.isEmpty() && posts.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    isLandscapeList && landscapes.isEmpty() -> {
                        Text(
                            "暂无内容",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    }
                    !isLandscapeList && posts.isEmpty() -> {
                        Text(
                            "暂无内容",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    }
                    isLandscapeList -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(landscapes, key = { it.landscapeId }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        context.startActivity(
                                            Intent(context, ScenicDetailActivity::class.java)
                                                .putExtra("landscapeId", item.landscapeId)
                                        )
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.address, color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(posts, key = { it.recomId }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        context.startActivity(
                                            Intent(context, PostDetailActivity::class.java)
                                                .putExtra("postId", item.recomId)
                                        )
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(item.title ?: "无标题", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        item.content.take(80) + if (item.content.length > 80) "…" else "",
                                        color = Color.DarkGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}
