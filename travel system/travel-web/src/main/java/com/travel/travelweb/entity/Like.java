package com.travel.travelweb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "`like`")
public class Like {

    @Id
    @Column(name = "LikeID", length = 30)
    private String likeId;

    @Column(name = "LinkUrl", length = 200)
    private String linkUrl;

    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "LikeTime")
    private LocalDateTime likeTime;

    public Like() {}

    public Like(String likeId, String linkUrl, String userId, LocalDateTime likeTime) {
        this.likeId = likeId;
        this.linkUrl = linkUrl;
        this.userId = userId;
        this.likeTime = likeTime;
    }

    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
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

    public LocalDateTime getLikeTime() {
        return likeTime;
    }

    public void setLikeTime(LocalDateTime likeTime) {
        this.likeTime = likeTime;
    }
}