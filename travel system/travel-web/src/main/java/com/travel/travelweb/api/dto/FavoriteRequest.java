package com.travel.travelweb.api.dto;

public class FavoriteRequest {
    private String targetId;
    private String targetType;

    public FavoriteRequest() {}

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
}
