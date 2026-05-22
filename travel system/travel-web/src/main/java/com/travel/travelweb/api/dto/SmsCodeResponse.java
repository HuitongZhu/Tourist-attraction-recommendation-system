package com.travel.travelweb.api.dto;

/** 发送验证码响应（供安卓弹窗展示） */
public class SmsCodeResponse {
    private String phone;
    private String smsCode;
    private int expiresInSeconds;

    public SmsCodeResponse() {}

    public SmsCodeResponse(String phone, String smsCode, int expiresInSeconds) {
        this.phone = phone;
        this.smsCode = smsCode;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSmsCode() { return smsCode; }
    public void setSmsCode(String smsCode) { this.smsCode = smsCode; }
    public int getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(int expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
}
