package com.example.travel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.delay
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
                                PageType.PUBLISH_POST -> startActivity(Intent(this@PublishScenicInfoActivity, PublishPostActivity::class.java))
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

private val SCENIC_LEVEL_OPTIONS = listOf("AAA", "AAAA", "AAAAA", "其他")

private enum class CoordInputMode { AUTO, MANUAL }

private fun parseCoordinate(text: String): Double? =
    text.trim().replace("，", ",").split(",").firstOrNull()?.trim()?.toDoubleOrNull()

private fun isValidChinaCoord(lat: Double, lng: Double): Boolean =
    lat in 3.0..54.0 && lng in 73.0..136.0

private data class PublishScenicParts(
    val title: okhttp3.RequestBody,
    val address: okhttp3.RequestBody,
    val content: okhttp3.RequestBody,
    val latitude: okhttp3.RequestBody,
    val longitude: okhttp3.RequestBody,
    val tel: okhttp3.RequestBody?,
    val openingTime: okhttp3.RequestBody?,
    val level: okhttp3.RequestBody,
    val image: okhttp3.MultipartBody.Part?
)

@OptIn(ExperimentalMaterial3Api::class)
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
    var coordMode by remember { mutableStateOf(CoordInputMode.AUTO) }
    var manualLatText by remember { mutableStateOf("") }
    var manualLngText by remember { mutableStateOf("") }
    var geocodeLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var levelMenuExpanded by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(scenicLocation, coordMode) {
        if (coordMode != CoordInputMode.AUTO) return@LaunchedEffect
        if (scenicLocation.trim().length <= 2) {
            latitude = null
            longitude = null
            return@LaunchedEffect
        }
        delay(600)
        geocodeLoading = true
        val coords = GeocodeHelper.fetchCoordinates(scenicLocation)
        geocodeLoading = false
        if (coords != null) {
            latitude = coords.first
            longitude = coords.second
        }
    }

    LaunchedEffect(manualLatText, manualLngText, coordMode) {
        if (coordMode != CoordInputMode.MANUAL) return@LaunchedEffect
        delay(300)
        val lat = parseCoordinate(manualLatText)
        val lng = parseCoordinate(manualLngText)
        latitude = lat
        longitude = lng
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

            Column {
                Text("景点图片", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "已选图片",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                            Text("点击上传图片", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }

            FormField("景点地点", scenicLocation, { scenicLocation = it }, "请输入详细地点")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("位置坐标", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { coordMode = CoordInputMode.AUTO },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (coordMode == CoordInputMode.AUTO) {
                                Color(0xFF1A56DB).copy(alpha = 0.12f)
                            } else {
                                Color.Transparent
                            }
                        )
                    ) {
                        Text("地址自动转换", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            coordMode = CoordInputMode.MANUAL
                            if (manualLatText.isBlank() && latitude != null) {
                                manualLatText = latitude.toString()
                                manualLngText = longitude?.toString() ?: ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (coordMode == CoordInputMode.MANUAL) {
                                Color(0xFF1A56DB).copy(alpha = 0.12f)
                            } else {
                                Color.Transparent
                            }
                        )
                    ) {
                        Text("手动输入", fontSize = 13.sp)
                    }
                }

                when (coordMode) {
                    CoordInputMode.AUTO -> {
                        if (geocodeLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("正在根据地址获取坐标…", fontSize = 12.sp, color = Color.Gray)
                            }
                        } else if (latitude != null && longitude != null &&
                            (latitude != 0.0 || longitude != 0.0)
                        ) {
                            Text(
                                text = "已获取坐标：纬度 ${String.format("%.6f", latitude)}，经度 ${String.format("%.6f", longitude)}",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        } else if (scenicLocation.trim().length > 2) {
                            Text(
                                text = "未能自动解析坐标，可切换到「手动输入」填写经纬度",
                                fontSize = 12.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                    CoordInputMode.MANUAL -> {
                        Column {
                            Text("纬度", fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedTextField(
                                value = manualLatText,
                                onValueChange = { manualLatText = it },
                                placeholder = { Text("例如 32.060255") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        Column {
                            Text("经度", fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedTextField(
                                value = manualLngText,
                                onValueChange = { manualLngText = it },
                                placeholder = { Text("例如 118.796877") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        Text(
                            text = "可从高德地图选点复制经纬度（纬度在前、经度在后）",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                val mapLat = latitude
                val mapLng = longitude
                val hasValidCoord = mapLat != null && mapLng != null &&
                    (mapLat != 0.0 || mapLng != 0.0) &&
                    isValidChinaCoord(mapLat, mapLng)

                Text("地图预览", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (hasValidCoord) {
                    ScenicMapView(
                        latitude = mapLat,
                        longitude = mapLng,
                        title = scenicName.ifBlank { "景点位置" },
                        address = scenicLocation.ifBlank { null },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ScenicMapView(
                        latitude = null,
                        longitude = null,
                        title = null,
                        address = scenicLocation.ifBlank { null },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

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
                        SCENIC_LEVEL_OPTIONS.forEach { option ->
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

            Spacer(modifier = Modifier.height(8.dp))

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
                    if (!UserSession.isLoggedIn()) {
                        Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    activity?.lifecycleScope?.launch {
                        try {
                            var lat = latitude
                            var lng = longitude
                            when (coordMode) {
                                CoordInputMode.MANUAL -> {
                                    lat = parseCoordinate(manualLatText)
                                    lng = parseCoordinate(manualLngText)
                                    if (lat == null || lng == null) {
                                        Toast.makeText(
                                            context,
                                            "请填写有效的纬度和经度",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@launch
                                    }
                                    if (!isValidChinaCoord(lat, lng)) {
                                        Toast.makeText(
                                            context,
                                            "经纬度超出有效范围，请检查是否填反或填错",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@launch
                                    }
                                }
                                CoordInputMode.AUTO -> {
                                    if (lat == null || lng == null || (lat == 0.0 && lng == 0.0)) {
                                        val coords = GeocodeHelper.fetchCoordinates(scenicLocation)
                                        if (coords != null) {
                                            lat = coords.first
                                            lng = coords.second
                                            latitude = lat
                                            longitude = lng
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "无法自动获取坐标，请填写更详细地址或改用手动输入经纬度",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@launch
                                        }
                                    }
                                }
                            }
                            val imagePart = selectedImageUri?.let {
                                MultipartUtil.imagePart(context, it)
                            }
                            val parts = PublishScenicParts(
                                title = MultipartUtil.textPart(scenicName),
                                address = MultipartUtil.textPart(scenicLocation),
                                content = MultipartUtil.textPart(scenicDetails.ifBlank { "" }),
                                latitude = MultipartUtil.textPart(lat.toString()),
                                longitude = MultipartUtil.textPart(lng.toString()),
                                tel = contact.takeIf { it.isNotBlank() }?.let { MultipartUtil.textPart(it) },
                                openingTime = openTime.takeIf { it.isNotBlank() }?.let { MultipartUtil.textPart(it) },
                                level = MultipartUtil.textPart(scenicLevel),
                                image = imagePart
                            )
                            val response = try {
                                NetworkClient.apiService.publishLandscapeApp(
                                    parts.title, parts.address, parts.content,
                                    parts.latitude, parts.longitude,
                                    parts.tel, parts.openingTime, parts.level, parts.image
                                )
                            } catch (_: retrofit2.HttpException) {
                                NetworkClient.apiService.publishLandscapeMultipart(
                                    parts.title, parts.address, parts.content,
                                    parts.latitude, parts.longitude,
                                    parts.tel, parts.openingTime, parts.level, parts.image
                                )
                            }
                            if (response.success && response.data != null) {
                                Toast.makeText(context, "发布成功，等待管理员审核", Toast.LENGTH_SHORT).show()
                                activity.finish()
                            } else {
                                Toast.makeText(context, response.message ?: "发布失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: retrofit2.HttpException) {
                            val msg = when (e.code()) {
                                405 -> "后端未更新：请 Rebuild 并重启 TravelWebApplication"
                                404 -> "接口不存在，请确认后端已启动"
                                else -> "请求失败 HTTP ${e.code()}"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
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
