package com.example.travel

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 共享的管理员顶部导航栏
 */
@Composable
fun AdminTopNavBar(showAvatar: Boolean = true, onProfileClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    Surface(
        color = Color(0xFF1A56DB),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .topBarSafePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "管理员功能",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (showAvatar) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF4A90E2), CircleShape)
                        .align(Alignment.CenterEnd)
                        .clickable { 
                            if (onProfileClick != null) {
                                onProfileClick()
                            } else {
                                context.startActivity(Intent(context, AdminProfileActivity::class.java))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "管",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 共享的管理员侧边栏
 */
@Composable
fun AdminSidebar(selectedModule: String, onModuleSelect: ((String) -> Unit)? = null) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(110.dp)
            .padding(top = 20.dp, start = 8.dp, end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text(
            "管理系统功能",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 22.sp,
            color = Color.Black
        )

        AdminSidebarItem(
            text = "用户账号管理",
            isSelected = selectedModule == "USER_MGMT",
            onClick = { 
                if (onModuleSelect != null) {
                    onModuleSelect("USER_MGMT")
                } else {
                    context.startActivity(Intent(context, AdminActivity::class.java))
                }
            }
        )

        AdminSidebarItem(
            text = "审核景点信息",
            isSelected = selectedModule == "SCENIC",
            onClick = { 
                if (selectedModule != "SCENIC") {
                    context.startActivity(Intent(context, ScenicReviewActivity::class.java))
                }
            }
        )

        AdminSidebarItem(
            text = "审核推荐帖",
            isSelected = selectedModule == "POST",
            onClick = { 
                if (selectedModule != "POST") {
                    context.startActivity(Intent(context, PostReviewActivity::class.java))
                }
            }
        )

        AdminSidebarItem(
            text = "审核评论",
            isSelected = selectedModule == "COMMENT",
            onClick = {
                if (selectedModule != "COMMENT") {
                    context.startActivity(Intent(context, CommentReviewActivity::class.java))
                }
            }
        )

        androidx.compose.material3.OutlinedButton(
            onClick = {
                UserSession.clear(context)
                Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "退出登录",
                fontSize = 15.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AdminSidebarItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (isSelected) Color(0xFF1A56DB) else Color.Gray,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        modifier = Modifier.clickable { onClick() }
    )
}

/** 审核页筛选：全部 / 通过 / 未审核 / 驳回（横向滑动） */
@Composable
fun ReviewFilterRow(selectedFilter: String, onFilterChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusChip("全部", selectedFilter == AdminReviewFilter.ALL) { onFilterChange(AdminReviewFilter.ALL) }
        StatusChip("通过", selectedFilter == AdminReviewFilter.APPROVED) { onFilterChange(AdminReviewFilter.APPROVED) }
        StatusChip("未审核", selectedFilter == AdminReviewFilter.PENDING) { onFilterChange(AdminReviewFilter.PENDING) }
        StatusChip("驳回", selectedFilter == AdminReviewFilter.REJECTED) { onFilterChange(AdminReviewFilter.REJECTED) }
    }
}

/**
 * 共享的状态筛选按钮 (Chip)
 */
@Composable
fun StatusChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF1A56DB) else Color(0xFFF0F2F5),
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
