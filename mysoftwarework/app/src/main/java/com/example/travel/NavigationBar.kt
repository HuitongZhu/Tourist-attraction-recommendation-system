package com.example.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 页面类型枚举
enum class PageType {
    HOME, RECOMMEND, PERSONAL, PUBLISH_SCENIC, PUBLISH_POST
}

/**
 * 统一顶部导航栏组件
 */
@Composable
fun TopNavBar(
    currentPage: PageType,
    onPageChange: (PageType) -> Unit,
    userName: String = "杨"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A56DB))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：系统名称
        Text(
            text = "旅游书",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 16.dp)
        )

        // 中间：导航项 (支持水平滚动)
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                "首页" to PageType.HOME,
                "推荐帖" to PageType.RECOMMEND,
                "发布景点" to PageType.PUBLISH_SCENIC,
                "发布推荐帖" to PageType.PUBLISH_POST,
                "我的" to PageType.PERSONAL
            )

            items.forEach { (label, type) ->
                val isSelected = currentPage == type
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color(0xFFE0EFFF),
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clickable { onPageChange(type) }
                )
            }
        }

        // 右侧：用户标识
        Text(
            text = userName,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
