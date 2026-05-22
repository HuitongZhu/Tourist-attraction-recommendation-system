package com.travel.travelweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "landcollect")
public class LandCollect {

    @Id
    @Column(name = "CollectID", length = 30)
    private String collectId;

    @Column(name = "LandscapeID", length = 30)
    private String landscapeId;

    @Column(name = "LinkUrl", length = 200)
    private String linkUrl;

    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "CollectTime")
    private LocalDateTime collectTime;

    public String getCollectId() {
        return collectId;
    }

    public void setCollectId(String collectId) {
        this.collectId = collectId;
    }

    public String getLandscapeId() {
        return landscapeId;
    }

    public void setLandscapeId(String landscapeId) {
        this.landscapeId = landscapeId;
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
