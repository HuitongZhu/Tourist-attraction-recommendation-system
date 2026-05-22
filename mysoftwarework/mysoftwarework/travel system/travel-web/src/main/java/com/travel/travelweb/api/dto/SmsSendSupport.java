package com.travel.travelweb.api.dto;

import com.travel.travelweb.service.AuthService;
import com.travel.travelweb.service.UserProfileService;

/** 统一生成短信验证码（供多个 dto 控制器调用） */
public final class SmsSendSupport {

    private static final int EXPIRE_SECONDS = 300;

    private SmsSendSupport() {}

    public static SmsCodeResponse send(
            AuthService authService,
            UserProfileService userProfileService,
            String phone,
            String type) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String normalized = phone.trim();
        if (!normalized.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("请输入正确的11位手机号");
        }
        String code = switch (type == null ? "" : type.toLowerCase()) {
            case "register" -> authService.sendRegisterSmsCode(normalized);
            case "password" -> userProfileService.sendPasswordCode(normalized);
            case "delete" -> userProfileService.sendDeleteCode(normalized);
            case "forgot", "login" -> authService.sendSmsCode(normalized);
            default -> authService.sendSmsCode(normalized);
        };
        return new SmsCodeResponse(normalized, code, EXPIRE_SECONDS);
    }
}
