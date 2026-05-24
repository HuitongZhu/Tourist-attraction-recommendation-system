package com.travel.travelweb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "collect")
public class Collect {

    @Id
    @Column(name = "CollectID", length = 30)
    private String collectId;

    @Column(name = "LinkUrl", length = 200)
    private String linkUrl;

    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "CollectTime")
    private LocalDateTime collectTime;

    public Collect() {}

    public Collect(String collectId, String linkUrl, String userId, LocalDateTime collectTime) {
        this.collectId = collectId;
        this.linkUrl = linkUrl;
        this.userId = userId;
        this.collectTime = collectTime;
    }

    public String getCollectId() {
        return collectId;
    }

    public void setCollectId(String collectId) {
        this.collectId = collectId;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(LocalDateTime collectTime) {
        this.collectTime = collectTime;
    }
}