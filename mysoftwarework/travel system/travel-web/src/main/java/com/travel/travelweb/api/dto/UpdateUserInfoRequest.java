package com.travel.travelweb.api.dto;

public class UpdateUserInfoRequest {
    private String userName;
    private String userType;

    public UpdateUserInfoRequest() {}

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
