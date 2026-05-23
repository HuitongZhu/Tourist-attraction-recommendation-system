package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

class MyScenicManagementActivity : ComponentActivity() {
    private val refreshTrigger = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val trigger by refreshTrigger
            TravelTheme {
                MyScenicManagementScreen(
                    refreshTrigger = trigger,
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTrigger.value++
    }
}

private fun auditStatusColor(status: String): Color = when (status) {
    "审核通过" -> Color(0xFF4CAF50)
    "审核未通过" -> Color(0xFFE91E63)
    "待审核", "审核中" -> Color(0xFFFF9800)
    else -> Color.Gray
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScenicManagementScreen(refreshTrigger: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    var landscapes by remember { mutableStateOf<List<LandscapeBackendResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var deleteTarget by remember { mutableStateOf<LandscapeBackendResponse?>(null) }

    fun reload() {
        activity?.lifecycleScope?.launch {
            loading = true
            try {
                val res = NetworkClient.apiService.getMyLandscapes()
                if (res.success) {
                    landscapes = res.data ?: emptyList()
                } else {
                    Toast.makeText(context, res.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(refreshTrigger) {
        if (!UserSession.isLoggedIn()) {
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
            loading = false
            return@LaunchedEffect
        }
        reload()
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除景点") },
            text = { Text("确定删除「${deleteTarget?.title}」？删除后不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    val id = deleteTarget?.landscapeId ?: return@TextButton
                    deleteTarget = null
                    activity?.lifecycleScope?.launch {
                        try {
                            val res = NetworkClient.apiService.deleteMyLandscape(id)
                            if (res.success) {
                                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                                reload()
                            } else {
                                Toast.makeText(context, res.message ?: "删除失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("删除", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
                title = { Text("我的景点信息") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                landscapes.isEmpty() -> Text(
                    text = "暂无发布的景点信息",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(landscapes, key = { it.landscapeId }) { scenic ->
                        MyScenicListItem(
                            scenic = scenic,
                            onEdit = {
                                context.startActivity(
                                    Intent(context, EditScenicActivity::class.java)
                                        .putExtra(EditScenicActivity.EXTRA_LANDSCAPE_ID, scenic.landscapeId)
                                )
                            },
                            onDelete = { deleteTarget = scenic }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyScenicListItem(
    scenic: LandscapeBackendResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = auditStatusColor(scenic.auditState)
    val editLabel = if (scenic.auditState == "审核未通过") "重新编辑" else "编辑"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(scenic.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (scenic.content.isNotBlank()) {
                        Text(
                            scenic.content,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Surface(color = statusColor, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        scenic.auditState,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("地点：${scenic.address}", fontSize = 13.sp, color = Color.Gray)
            scenic.level?.let { Text("等级：$it", fontSize = 13.sp, color = Color.Gray) }
            scenic.publishTime?.let { Text("发布时间：$it", fontSize = 13.sp, color = Color.Gray) }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDelete, modifier = Modifier.padding(end = 8.dp)) {
                    Text("删除")
                }
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
                ) {
                    Text(editLabel)
                }
            }
        }
    }
}
