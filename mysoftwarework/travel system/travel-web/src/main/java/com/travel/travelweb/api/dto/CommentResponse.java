package com.travel.travelweb.api.dto;

import java.time.LocalDateTime;

public class CommentResponse {
    
    private String commentId;
    private String userId;
    private String userName;
    private String content;
    private LocalDateTime publishTime;
    private String landscapeId;
    private String postId;

    public CommentResponse() {
    }

    public CommentResponse(String commentId, String userId, String userName, String content, 
                          LocalDateTime publishTime, String landscapeId, String postId) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.publishTime = publishTime;
        this.landscapeId = landscapeId;
        this.postId = postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public String getLandscapeId() {
        return landscapeId;
    }

    public void setLandscapeId(String landscapeId) {
        this.landscapeId = landscapeId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
