package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import kotlinx.coroutines.launch

class ScenicReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTheme {
                Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    AdminTopNavBar()

                    Row(modifier = Modifier.fillMaxSize()) {
                        AdminSidebar(selectedModule = "SCENIC")

                        Box(modifier = Modifier.weight(1f).padding(top = 16.dp, end = 16.dp, bottom = 16.dp)) {
                            ScenicReviewMainContent()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenicReviewMainContent() {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    var scenicInfos by remember { mutableStateOf<List<LandscapeResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    var selectedStatus by remember { mutableStateOf("未审核") }

    LaunchedEffect(searchText, refreshTrigger, selectedStatus) {
        isLoading = true
        try {
            val response = NetworkClient.apiService.getLandscapes(
                keyword = if (searchText.isEmpty()) null else searchText,
                status = selectedStatus
            )
            if (response.success) {
                scenicInfos = response.data?.content ?: emptyList()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("景点信息审核", fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("待审核", selectedStatus == "未审核") { selectedStatus = "未审核" }
            StatusChip("已通过", selectedStatus == "审核通过") { selectedStatus = "审核通过" }
            StatusChip("已驳回", selectedStatus == "审核未通过") { selectedStatus = "审核未通过" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search", fontSize = 18.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(24.dp)) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF1A56DB)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                items(scenicInfos) { scenic ->
                    ScenicReviewItem(scenicInfo = scenic, onAudit = { refreshTrigger++ })
                }
            }
        }
    }
}

@Composable
fun ScenicReviewItem(scenicInfo: LandscapeResponse, onAudit: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column {
            AsyncImage(
                model = "https://via.placeholder.com/400x200.png?text=${scenicInfo.title}",
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text(text = scenicInfo.title, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text(text = "提交人: ${scenicInfo.creator?.username ?: "未知"}", fontSize = 18.sp, color = Color.Gray)
                    }
                    val statusColor = when(scenicInfo.status) {
                        "未审核" -> Color(0xFFFF9800)
                        "审核通过" -> Color(0xFF2E7D32)
                        "审核未通过" -> Color.Red
                        else -> Color.Gray
                    }
                    Text(text = scenicInfo.status, fontSize = 18.sp, color = statusColor, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(12.dp))
                Text(text = scenicInfo.content, fontSize = 18.sp, color = Color.Gray, maxLines = 2)
                Spacer(Modifier.height(20.dp))
                
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { 
                            val intent = Intent(context, ScenicDetailActivity::class.java)
                            intent.putExtra("landscapeId", scenicInfo.id)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        border = BorderStroke(1.dp, Color(0xFF1A56DB))
                    ) {
                        Icon(Icons.Default.Info, null, Modifier.size(20.dp), tint = Color(0xFF1A56DB))
                        Spacer(Modifier.width(6.dp))
                        Text("查看详情", color = Color(0xFF1A56DB), fontSize = 18.sp)
                    }

                    if (scenicInfo.status == "未审核") {
                        Button(
                            onClick = { 
                                scope.launch {
                                    try {
                                        val res = NetworkClient.apiService.auditLandscape(scenicInfo.id, AuditRequest(true, "审核通过"))
                                        if (res.success) {
                                            Toast.makeText(context, "审核通过", Toast.LENGTH_SHORT).show()
                                            onAudit()
                                        }
                                    } catch (e: Exception) { }
                                }
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB)),
                            shape = RoundedCornerShape(27.dp)
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("通过", fontSize = 18.sp)
                        }
                    }
                }

                if (scenicInfo.status == "未审核") {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { 
                            scope.launch {
                                try {
                                    val res = NetworkClient.apiService.auditLandscape(scenicInfo.id, AuditRequest(false, "驳回审核"))
                                    if (res.success) {
                                        Toast.makeText(context, "已驳回", Toast.LENGTH_SHORT).show()
                                        onAudit()
                                    }
                                } catch (e: Exception) { }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("驳回审核", color = Color.Red, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
