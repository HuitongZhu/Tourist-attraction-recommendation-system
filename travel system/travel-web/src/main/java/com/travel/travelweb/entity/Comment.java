package com.travel.travelweb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "`comment`")
public class Comment {

    @Id
    @Column(name = "CommentID", length = 30)
    private String commentId;

    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "Content", columnDefinition = "text")
    private String content;

    @Column(name = "PublishTime")
    private LocalDateTime publishTime;

    public Comment() {}

    public Comment(String commentId, String userId, String content, LocalDateTime publishTime) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.publishTime = publishTime;
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
}