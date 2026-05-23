package com.travel.travelweb.service;

/** 短信验证码 Redis 缓存分类 */
public enum SmsCodeType {
    AUTH("auth"),
    PASSWORD("password"),
    DELETE("delete");

    private final String keySegment;

    SmsCodeType(String keySegment) {
        this.keySegment = keySegment;
    }

    public String keySegment() {
        return keySegment;
    }
}
