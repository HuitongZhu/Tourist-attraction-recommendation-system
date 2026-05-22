package com.travel.travelweb.api.dto;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 评论审核通过记录（评论表无审核字段时的运行时标记）
 */
public final class CommentAuditStore {

    private static final Set<String> APPROVED_IDS = ConcurrentHashMap.newKeySet();

    private CommentAuditStore() {}

    public static boolean isApproved(String commentId) {
        return commentId != null && APPROVED_IDS.contains(commentId);
    }

    public static void markApproved(String commentId) {
        if (commentId != null && !commentId.isBlank()) {
            APPROVED_IDS.add(commentId);
        }
    }

    public static void unmark(String commentId) {
        if (commentId != null) {
            APPROVED_IDS.remove(commentId);
        }
    }

    public static String commentAuditState(String commentId) {
        return isApproved(commentId) ? "审核通过" : "未审核";
    }
}
