package com.travel.travelweb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "postlike")
public class PostLike {

    @Id
    @Column(name = "LikeID", length = 30)
    private String likeId;

    @Column(name = "RecomID", length = 30, nullable = false)
    private String recomId;

    @Column(name = "userID", length = 30, nullable = false)
    private String userId;

    @Column(name = "LinkUrl", length = 200)
    private String linkUrl;

    @Column(name = "LikeTime")
    private LocalDateTime likeTime;

    public PostLike() {}

    public String getLikeId() { return likeId; }
    public void setLikeId(String likeId) { this.likeId = likeId; }

    public String getRecomId() { return recomId; }
    public void setRecomId(String recomId) { this.recomId = recomId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }

    public LocalDateTime getLikeTime() { return likeTime; }
    public void setLikeTime(LocalDateTime likeTime) { this.likeTime = likeTime; }
}