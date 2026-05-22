package com.travel.travelweb.api.dto;

public final class InteractionRequestSupport {

    private InteractionRequestSupport() {
    }

    public static String resolveTargetId(FavoriteRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getTargetId() != null && !request.getTargetId().isBlank()) {
            return request.getTargetId();
        }
        if (request.getLandscapeId() != null && !request.getLandscapeId().isBlank()) {
            return request.getLandscapeId();
        }
        if (request.getPostId() != null && !request.getPostId().isBlank()) {
            return request.getPostId();
        }
        return null;
    }

    public static String resolveTargetId(LikeRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getTargetId() != null && !request.getTargetId().isBlank()) {
            return request.getTargetId();
        }
        if (request.getLandscapeId() != null && !request.getLandscapeId().isBlank()) {
            return request.getLandscapeId();
        }
        if (request.getPostId() != null && !request.getPostId().isBlank()) {
            return request.getPostId();
        }
        return null;
    }

    public static boolean isLandscapeType(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            return false;
        }
        String t = targetType.trim().toUpperCase();
        return "LANDSCAPE".equals(t) || "LANDSCAPES".equals(t);
    }

    public static boolean isPostType(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            return false;
        }
        String t = targetType.trim().toUpperCase();
        return "POST".equals(t) || "POSTS".equals(t) || "RECOMMENDATION".equals(t);
    }
}
