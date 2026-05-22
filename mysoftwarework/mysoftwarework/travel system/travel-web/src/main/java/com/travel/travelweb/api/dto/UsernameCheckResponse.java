package com.travel.travelweb.api.dto;

/** 注册时用户名是否可用 */
public class UsernameCheckResponse {
    private boolean available;
    private String message;

    public UsernameCheckResponse() {}

    public UsernameCheckResponse(boolean available, String message) {
        this.available = available;
        this.message = message;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
