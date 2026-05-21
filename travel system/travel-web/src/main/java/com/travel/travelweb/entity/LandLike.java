package com.travel.travelweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "landlike")
public class LandLike {

    @Id
    @Column(name = "LikeID", length = 30)
    private String likeId;

    @Column(name = "LandscapeID", length = 30)
    private String landscapeId;

    @Column(name = "LinkUrl", length = 200)
    private String linkUrl;

    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "LikeTime")
    private LocalDateTime likeTime;

    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
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

    public LocalDateTime getLikeTime() {
        return likeTime;
    }

    public void setLikeTime(LocalDateTime likeTime) {
        this.likeTime = likeTime;
    }
}
