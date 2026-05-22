package com.travel.travelweb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "postcollect")
public class PostCollect {

    @Id
    @Column(name = "CollectID", length = 30)
    private String collectId;

    @Column(name = "RecomID", length = 30, nullable = false)
    private String recomId;

    @Column(name = "userID", length = 30, nullable = false)
    private String userId;

    @Column(name = "LinkUrl", length = 200)
    private String linkUrl;

    @Column(name = "CollectTime")
    private LocalDateTime collectTime;

    public PostCollect() {}

    public String getCollectId() { return collectId; }
    public void setCollectId(String collectId) { this.collectId = collectId; }

    public String getRecomId() { return recomId; }
    public void setRecomId(String recomId) { this.recomId = recomId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }

    public LocalDateTime getCollectTime() { return collectTime; }
    public void setCollectTime(LocalDateTime collectTime) { this.collectTime = collectTime; }
}