package com.travel.travelweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendationpost")
public class RecommendationPost {

    @Id
    @Column(name = "RecomID", length = 30)
    private String recomId;

    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "Title", length = 100)
    private String title;

    @Column(name = "LandscapeID", length = 30)
    private String landscapeId;

    @Column(name = "Tag", length = 100)
    private String tag;

    @Column(name = "Content", columnDefinition = "text")
    private String content;

    @Column(name = "PublishTime")
    private LocalDateTime publishTime;

    @Column(name = "AuditState", length = 20)
    private String auditState;

    public String getRecomId() {
        return recomId;
    }

    public void setRecomId(String recomId) {
        this.recomId = recomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLandscapeId() {
        return landscapeId;
    }

    public void setLandscapeId(String landscapeId) {
        this.landscapeId = landscapeId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public String getAuditState() {
        return auditState;
    }

    public void setAuditState(String auditState) {
        this.auditState = auditState;
    }
}
