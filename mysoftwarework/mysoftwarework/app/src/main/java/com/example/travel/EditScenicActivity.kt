package com.example.travel

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.launch

class EditScenicActivity : ComponentActivity() {
    companion object {
        const val EXTRA_LANDSCAPE_ID = "landscape_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val landscapeId = intent.getStringExtra(EXTRA_LANDSCAPE_ID)
        if (landscapeId.isNullOrBlank()) {
            Toast.makeText(this, "景点不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                EditScenicScreen(landscapeId = landscapeId, onBack = { finish() })
            }
        }
    }
}

private val EDIT_LEVEL_OPTIONS = listOf("AAA", "AAAA", "AAAAA", "其他")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScenicScreen(landscapeId: String, onBack: () -> Unit) {
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
    var levelMenuExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(landscapeId) {
        loading = true
        try {
            val res = NetworkClient.apiService.getMyLandscapeById(landscapeId)
            if (res.success && res.data != null) {
                val d = res.data
                scenicName = d.title
                scenicLocation = d.address
                contact = d.landscapeTel.orEmpty()
                openTime = d.openingTime.orEmpty()
                scenicLevel = d.level.orEmpty()
                scenicDetails = d.content
                latitude = d.latitude
                longitude = d.longitude
            } else {
                Toast.makeText(context, res.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            activity?.finish()
        } finally {
            loading = false
        }
    }

    LaunchedEffect(scenicLocation) {
        if (scenicLocation.trim().length <= 2) return@LaunchedEffect
        kotlinx.coroutines.delay(600)
        val coords = GeocodeHelper.fetchCoordinates(scenicLocation)
        if (coords != null) {
            latitude = coords.first
            longitude = coords.second
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
                title = { Text("编辑景点信息") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Surface(
                color = Color(0xFFFFF9C4),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "修改后将重新提交管理员审核，审核通过后对外展示",
                    fontSize = 14.sp,
                    color = Color(0xFF8B8000),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            FormField("景点名称", scenicName, { scenicName = it }, "请输入景点名称")
            FormField("景点地点", scenicLocation, { scenicLocation = it }, "请输入详细地点")
            FormField("联系方式", contact, { contact = it }, "请输入联系电话")
            FormField("开放时间", openTime, { openTime = it }, "请输入开放时间")

            Column {
                Text("景点等级", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = levelMenuExpanded,
                    onExpandedChange = { levelMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = scenicLevel,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("请选择景点等级") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = levelMenuExpanded,
                        onDismissRequest = { levelMenuExpanded = false }
                    ) {
                        EDIT_LEVEL_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    scenicLevel = option
                                    levelMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
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
                    if (scenicName.isBlank() || scenicLocation.isBlank()) {
                        Toast.makeText(context, "请填写景点名称和地点", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (scenicLevel.isBlank()) {
                        Toast.makeText(context, "请选择景点等级", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    submitting = true
                    activity?.lifecycleScope?.launch {
                        try {
                            var lat = latitude
                            var lng = longitude
                            if (lat == null || lng == null || (lat == 0.0 && lng == 0.0)) {
                                val coords = GeocodeHelper.fetchCoordinates(scenicLocation)
                                if (coords != null) {
                                    lat = coords.first
                                    lng = coords.second
                                } else {
                                    Toast.makeText(
                                        context,
                                        "无法获取地点坐标，请填写更详细地址",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    submitting = false
                                    return@launch
                                }
                            }
                            val request = LandscapeRequest(
                                title = scenicName.trim(),
                                content = scenicDetails.trim(),
                                address = scenicLocation.trim(),
                                latitude = lat,
                                longitude = lng,
                                contactPhone = contact.takeIf { it.isNotBlank() },
                                openingTime = openTime.takeIf { it.isNotBlank() },
                                level = scenicLevel
                            )
                            val res = NetworkClient.apiService.updateMyLandscape(landscapeId, request)
                            if (res.success) {
                                Toast.makeText(context, "已提交审核，请等待管理员处理", Toast.LENGTH_SHORT).show()
                                activity.finish()
                            } else {
                                Toast.makeText(context, res.message ?: "保存失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            submitting = false
                        }
                    }
                },
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
            ) {
                Text(if (submitting) "提交中…" else "提交审核")
            }
        }
    }
}
