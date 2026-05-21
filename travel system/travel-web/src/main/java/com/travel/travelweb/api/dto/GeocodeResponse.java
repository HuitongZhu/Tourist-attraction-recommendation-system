package com.travel.travelweb.api.dto;

public class GeocodeResponse {
    private Double latitude;
    private Double longitude;
    private String address;

    public GeocodeResponse() {}

    public GeocodeResponse(Double latitude, Double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
