package com.example.travel

/** 管理员审核筛选：全部 / 通过 / 未审核 */
object AdminReviewFilter {
    const val ALL = "all"
    const val APPROVED = "approved"
    const val PENDING = "pending"

    fun isPendingStatus(status: String?): Boolean {
        if (status.isNullOrBlank()) return true
        return status == "待审核" || status == "审核中" || status == "未审核"
    }

    fun isApprovedStatus(status: String?): Boolean = status == "审核通过"

    fun displayStatus(status: String?): String = when {
        isPendingStatus(status) -> "未审核"
        status.isNullOrBlank() -> "未审核"
        else -> status
    }

    fun auditSortOrder(status: String?): Int = when {
        isPendingStatus(status) -> 0
        isApprovedStatus(status) -> 1
        status == "审核未通过" -> 2
        else -> 0
    }
}
