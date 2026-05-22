package com.travel.travelweb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "landscape")
public class Landscape {

    @Id
    @Column(name = "LandscapeID", length = 30)
    private String landscapeId;

    @Column(name = "userID", length = 30)
    private String userId;

    @Column(name = "Title", length = 50)
    private String title;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "Address", length = 100)
    private String address;

    @Column(name = "LandscapeTel", length = 20)
    private String landscapeTel;

    @Column(name = "OpeningTime", length = 50)
    private String openingTime;

    @Column(name = "Level", length = 20)
    private String level;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(name = "Latitude", columnDefinition = "DECIMAL(10,6)")
    private Double latitude;

    @Column(name = "Longitude", columnDefinition = "DECIMAL(10,6)")
    private Double longitude;

    @Column(name = "AuditState", length = 20)
    private String auditState;

    @Column(name = "PublishTime")
    private LocalDateTime publishTime;

    @Column(name = "AuditTime")
    private LocalDateTime auditTime;

    public String getLandscapeId() {
        return landscapeId;
    }

    public void setLandscapeId(String landscapeId) {
        this.landscapeId = landscapeId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLandscapeTel() {
        return landscapeTel;
    }

    public void setLandscapeTel(String landscapeTel) {
        this.landscapeTel = landscapeTel;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAuditState() {
        return auditState;
    }

    public void setAuditState(String auditState) {
        this.auditState = auditState;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }
}
