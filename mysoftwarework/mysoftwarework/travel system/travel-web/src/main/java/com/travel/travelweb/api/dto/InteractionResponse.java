package com.travel.travelweb.api.dto;

public class InteractionResponse {
    private String id;
    private String targetType;
    private String landscapeId;
    private String postId;
    private String linkUrl;

    public InteractionResponse() {}

    public InteractionResponse(String id, String targetType, String landscapeId, String postId, String linkUrl) {
        this.id = id;
        this.targetType = targetType;
        this.landscapeId = landscapeId;
        this.postId = postId;
        this.linkUrl = linkUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getLandscapeId() { return landscapeId; }
    public void setLandscapeId(String landscapeId) { this.landscapeId = landscapeId; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
}
