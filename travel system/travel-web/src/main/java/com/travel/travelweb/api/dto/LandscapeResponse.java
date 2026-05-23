package com.travel.travelweb.api.dto;

import java.time.LocalDateTime;

public class LandscapeResponse {
    private String landscapeId;
    private String title;
    private String content;
    private String address;
    private Double latitude;
    private Double longitude;
    private String tel;
    private String openingTime;
    private String level;
    private String imagePath;
    private String auditState;
    private LocalDateTime publishTime;
    private long likeCount;
    private long favoriteCount;
    private long commentCount;

    public LandscapeResponse() {}

    public String getLandscapeId() { return landscapeId; }
    public void setLandscapeId(String landscapeId) { this.landscapeId = landscapeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getAuditState() { return auditState; }
    public void setAuditState(String auditState) { this.auditState = auditState; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(long favoriteCount) { this.favoriteCount = favoriteCount; }
    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
}
