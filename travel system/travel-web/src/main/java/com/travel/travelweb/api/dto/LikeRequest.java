package com.travel.travelweb.api.dto;

public class LikeRequest {
    private String targetId;
    private String targetType;
    private String landscapeId;
    private String postId;
    private String linkUrl;

    public LikeRequest() {}

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getLandscapeId() { return landscapeId; }
    public void setLandscapeId(String landscapeId) { this.landscapeId = landscapeId; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
}
