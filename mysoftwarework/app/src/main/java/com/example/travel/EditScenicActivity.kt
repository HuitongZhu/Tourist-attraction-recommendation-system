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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.travel.ui.theme.TravelTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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
    var latitudeText by remember { mutableStateOf("") }
    var longitudeText by remember { mutableStateOf("") }
    var scenicImage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var levelMenuExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

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
                latitudeText = d.latitude?.toString() ?: ""
                longitudeText = d.longitude?.toString() ?: ""
                scenicImage = d.imagePath
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

    LaunchedEffect(scenicLocation, scenicName) {
        if (scenicLocation.trim().length <= 2) return@LaunchedEffect
        delay(600)
        val coords = GeocodeHelper.fetchCoordinates(scenicLocation, scenicName)
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

            // 景点图片上传区域
            Column {
                Text("景点图片", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    // 显示已选图片或现有图片
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "已选图片",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (scenicImage != null) {
                        AsyncImage(
                            model = "${NetworkClient.BASE_URL}${scenicImage}",
                            contentDescription = "当前图片",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // 始终显示灰色覆盖层和加号
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("点击修改图片", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }

            FormField("景点地点", scenicLocation, { scenicLocation = it }, "请输入详细地点")
            FormField("联系方式", contact, { contact = it }, "请输入联系电话")
            FormField("开放时间", openTime, { openTime = it }, "请输入开放时间")

            // 经纬度输入框
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("纬度", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = {
                            latitudeText = it
                            latitude = it.toDoubleOrNull()
                        },
                        placeholder = { Text("请输入纬度") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("经度", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = longitudeText,
                        onValueChange = {
                            longitudeText = it
                            longitude = it.toDoubleOrNull()
                        },
                        placeholder = { Text("请输入经度") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            }

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
                            // 如果选择了新图片，使用multipart接口
                            if (selectedImageUri != null) {
                                val file = selectedImageUri?.let { uri ->
                                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                        val tempFile = File(context.cacheDir, "temp_image.jpg")
                                        tempFile.outputStream().use { outputStream ->
                                            inputStream.copyTo(outputStream)
                                        }
                                        tempFile
                                    }
                                }

                                if (file != null) {
                                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)

                                    val res = NetworkClient.apiService.updateMyLandscapeWithImage(
                                        landscapeId = landscapeId,
                                        title = scenicName.trim().toRequestBody(),
                                        content = scenicDetails.trim().toRequestBody(),
                                        address = scenicLocation.trim().toRequestBody(),
                                        latitude = latitude?.toString()?.toRequestBody(),
                                        longitude = longitude?.toString()?.toRequestBody(),
                                        contactPhone = contact.takeIf { it.isNotBlank() }?.toRequestBody(),
                                        openingTime = openTime.takeIf { it.isNotBlank() }?.toRequestBody(),
                                        level = scenicLevel.toRequestBody(),
                                        image = imagePart
                                    )
                                    if (res.success) {
                                        Toast.makeText(context, "已提交审核，请等待管理员处理", Toast.LENGTH_SHORT).show()
                                        activity.finish()
                                    } else {
                                        Toast.makeText(context, res.message ?: "保存失败", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "图片处理失败", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // 没有选择新图片，使用原有的JSON接口
                                val request = LandscapeRequest(
                                    title = scenicName.trim(),
                                    content = scenicDetails.trim(),
                                    address = scenicLocation.trim(),
                                    latitude = latitude,
                                    longitude = longitude,
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
