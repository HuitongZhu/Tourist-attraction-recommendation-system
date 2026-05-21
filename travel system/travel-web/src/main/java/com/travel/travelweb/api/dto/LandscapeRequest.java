package com.travel.travelweb.api.dto;

public class LandscapeRequest {
    private String title;
    private String content;
    private String address;
    private Double latitude;
    private Double longitude;
    private String tel;
    private String openingTime;
    private String level;

    public LandscapeRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
