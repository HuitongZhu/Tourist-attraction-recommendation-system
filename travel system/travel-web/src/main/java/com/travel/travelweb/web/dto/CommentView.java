package com.travel.travelweb.web.dto;

import java.time.LocalDateTime;

public class CommentView {
    private String commentId;
    private String userId;
    private String userName;
    private String content;
    private LocalDateTime publishTime;
    private String refType;
    private String refId;
    private String refTitle;

    public CommentView() {
    }

    public CommentView(String commentId, String userId, String userName, String content, 
                       LocalDateTime publishTime, String refType, String refId, String refTitle) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.publishTime = publishTime;
        this.refType = refType;
        this.refId = refId;
        this.refTitle = refTitle;
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

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getRefTitle() {
        return refTitle;
    }

    public void setRefTitle(String refTitle) {
        this.refTitle = refTitle;
    }
}