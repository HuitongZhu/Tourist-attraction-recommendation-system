package com.travel.travelweb.api.dto;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.service.UserProfileService;

/**
 * 账号注销相关 API（供安卓端 DeleteAccountDialog 使用）
 */
@RestController
@RequestMapping("/api/users")
public class UserAccountApiController {

    private final UserProfileService userProfileService;

    public UserAccountApiController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /** 注销前密码验证 */
    @PostMapping(value = "/verify-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<Void>> verifyPassword(
            @RequestParam("password") String password,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("请输入密码"));
        }
        boolean valid = userProfileService.verifyAndDelete(userId, "password", password.trim());
        if (!valid) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "密码错误"));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** 注销前短信验证码验证 */
    @PostMapping(value = "/verify-sms-code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<Void>> verifyDeleteSmsCode(
            @RequestParam("phone") String phone,
            @RequestParam("code") String code,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        if (phone == null || phone.isBlank() || code == null || code.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("请输入手机号和验证码"));
        }
        try {
            String boundPhone = userProfileService.getPhone(userId);
            if (!boundPhone.equals(phone.trim())) {
                return ResponseEntity.ok(ApiResponse.error("手机号与账号绑定的手机号不一致"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
        boolean valid = userProfileService.verifyAndDelete(userId, "sms", code.trim());
        if (!valid) {
            return ResponseEntity.ok(ApiResponse.error("验证码错误或已过期"));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** 确认注销当前登录用户（需先通过 verify-password 或 verify-sms-code） */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        try {
            userProfileService.deleteAccount(userId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
