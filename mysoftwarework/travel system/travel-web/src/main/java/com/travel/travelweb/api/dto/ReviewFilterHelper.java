package com.travel.travelweb.api.dto;


/**
 * 管理员审核列表筛选与排序：未审核 → 通过 → 驳回
 */
public final class ReviewFilterHelper {

    public static final String FILTER_ALL = "all";
    public static final String FILTER_APPROVED = "approved";
    public static final String FILTER_PENDING = "pending";

    private ReviewFilterHelper() {}

    public static boolean isPending(String auditState) {
        if (auditState == null || auditState.isBlank()) {
            return true;
        }
        String s = auditState.trim();
        return "待审核".equals(s) || "审核中".equals(s) || "未审核".equals(s);
    }

    public static boolean isApproved(String auditState) {
        return "审核通过".equals(auditState);
    }

    public static boolean isRejected(String auditState) {
        return "审核未通过".equals(auditState);
    }

    public static int sortOrder(String auditState) {
        if (isPending(auditState)) {
            return 0;
        }
        if (isApproved(auditState)) {
            return 1;
        }
        if (isRejected(auditState)) {
            return 2;
        }
        return 0;
    }

    public static String mapFilter(String filter) {
        if (filter == null || filter.isBlank() || FILTER_ALL.equalsIgnoreCase(filter)) {
            return FILTER_ALL;
        }
        if (FILTER_APPROVED.equalsIgnoreCase(filter) || "通过".equals(filter)) {
            return FILTER_APPROVED;
        }
        if (FILTER_PENDING.equalsIgnoreCase(filter) || "未审核".equals(filter)) {
            return FILTER_PENDING;
        }
        return FILTER_ALL;
    }

    public static boolean matchesFilter(String auditState, String filter) {
        String f = mapFilter(filter);
        if (FILTER_ALL.equals(f)) {
            return true;
        }
        if (FILTER_APPROVED.equals(f)) {
            return isApproved(auditState);
        }
        if (FILTER_PENDING.equals(f)) {
            return isPending(auditState);
        }
        return true;
    }

    public static String resolveAuditStatus(AuditRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            return request.getStatus().trim();
        }
        if (Boolean.TRUE.equals(request.getApproved())) {
            return "审核通过";
        }
        if (Boolean.FALSE.equals(request.getApproved())) {
            return "审核未通过";
        }
        return null;
    }
}
