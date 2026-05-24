package com.travel.travelweb.api.dto;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.service.AuthService;
import com.travel.travelweb.service.UserProfileService;

/**
 * 统一发送验证码（返回验证码供安卓弹窗展示）
 * type: login | register | password | delete | forgot
 */
@RestController
@RequestMapping("/api/sms")
public class SmsApiController {

    private static final int EXPIRE_SECONDS = 300;

    private final AuthService authService;
    private final UserProfileService userProfileService;

    public SmsApiController(AuthService authService, UserProfileService userProfileService) {
        this.authService = authService;
        this.userProfileService = userProfileService;
    }

    @PostMapping(value = "/send-code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<SmsCodeResponse>> sendCode(
            @RequestParam("phone") String phone,
            @RequestParam(value = "type", defaultValue = "login") String type) {
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("手机号不能为空"));
        }
        String normalized = phone.trim();
        try {
            switch (type == null ? "" : type.toLowerCase()) {
                case "register" -> authService.sendRegisterSmsCode(normalized);
                case "password" -> userProfileService.sendPasswordCode(normalized);
                case "delete" -> userProfileService.sendDeleteCode(normalized);
                case "forgot", "login" -> authService.sendSmsCode(normalized);
                default -> authService.sendSmsCode(normalized);
            }
            String code = "******";
            SmsCodeResponse data = new SmsCodeResponse(normalized, code, EXPIRE_SECONDS);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
