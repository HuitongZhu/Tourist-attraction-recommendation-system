package com.travel.travelweb.api.dto;

public class LandscapeRequest {
    private String title;
    private String content;
    private String address;
    private Double latitude;
    private Double longitude;
    private String tel;
    /** 安卓端字段名，与 tel 二选一 */
    private String contactPhone;
    private String openingTime;
    private String level;

    public LandscapeRequest() {}

    /** 兼容 contactPhone / tel */
    public String resolveTel() {
        if (tel != null && !tel.isBlank()) {
            return tel.trim();
        }
        if (contactPhone != null && !contactPhone.isBlank()) {
            return contactPhone.trim();
        }
        return null;
    }

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
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
