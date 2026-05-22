package com.travel.travelweb.api.dto;

import java.time.LocalDateTime;

public class PostResponse {
    private String recomId;
    private String userId;
    private String userName;
    private String title;
    private String landscapeId;
    private String landscapeTitle;
    private String tag;
    private String content;
    private String auditState;
    private LocalDateTime publishTime;
    private long likeCount;
    private long commentCount;

    public PostResponse() {}

    public String getRecomId() { return recomId; }
    public void setRecomId(String recomId) { this.recomId = recomId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLandscapeId() { return landscapeId; }
    public void setLandscapeId(String landscapeId) { this.landscapeId = landscapeId; }
    public String getLandscapeTitle() { return landscapeTitle; }
    public void setLandscapeTitle(String landscapeTitle) { this.landscapeTitle = landscapeTitle; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuditState() { return auditState; }
    public void setAuditState(String auditState) { this.auditState = auditState; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
}
