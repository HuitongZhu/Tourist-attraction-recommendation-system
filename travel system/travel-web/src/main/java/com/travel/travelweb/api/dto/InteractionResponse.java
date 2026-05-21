package com.travel.travelweb.api.dto;

public class InteractionResponse {
    private String id;
    private String targetId;
    private String targetType;
    private String userId;

    public InteractionResponse() {}

    public InteractionResponse(String id, String targetId, String targetType, String userId) {
        this.id = id;
        this.targetId = targetId;
        this.targetType = targetType;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
