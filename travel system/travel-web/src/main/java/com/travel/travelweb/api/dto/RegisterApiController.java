package com.travel.travelweb.api.dto;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.service.AuthService;
import com.travel.travelweb.service.UserProfileService;

/**
 * 注册相关校验（供安卓端）
 */
@RestController
@RequestMapping("/api/register")
public class RegisterApiController {

    private final SysUserRepository sysUserRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final AuthService authService;
    private final UserProfileService userProfileService;

    public RegisterApiController(
            SysUserRepository sysUserRepository,
            OrdinaryUserRepository ordinaryUserRepository,
            AuthService authService,
            UserProfileService userProfileService) {
        this.sysUserRepository = sysUserRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.authService = authService;
        this.userProfileService = userProfileService;
    }

    /** 发送验证码（备用路径，与 /api/sms-send-code 相同逻辑） */
    @PostMapping(value = "/sms-code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<SmsCodeResponse>> sendCode(
            @RequestParam("phone") String phone,
            @RequestParam(value = "type", defaultValue = "login") String type) {
        try {
            SmsCodeResponse data = SmsSendSupport.send(authService, userProfileService, phone, type);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /** 检查用户名是否可用 */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<UsernameCheckResponse>> checkUsername(
            @RequestParam("username") String username) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("用户名不能为空"));
        }
        String name = username.trim();
        if (name.length() < 2) {
            return ResponseEntity.ok(ApiResponse.success(
                    new UsernameCheckResponse(false, "用户名至少2个字符")));
        }
        try {
            authService.validateUserName(name);
            return ResponseEntity.ok(ApiResponse.success(
                    new UsernameCheckResponse(true, "用户名可用")));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("已被")) {
                return ResponseEntity.ok(ApiResponse.success(
                        new UsernameCheckResponse(false, msg)));
            }
            return ResponseEntity.ok(ApiResponse.error(msg));
        }
    }

    /** 检查手机号是否已注册 */
    @GetMapping("/check-account")
    public ResponseEntity<ApiResponse<UsernameCheckResponse>> checkAccount(
            @RequestParam("account") String account) {
        if (account == null || account.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("手机号不能为空"));
        }
        String phone = account.trim();
        if (!phone.matches("^1\\d{10}$")) {
            return ResponseEntity.ok(ApiResponse.error("请输入正确的手机号格式"));
        }
        boolean exists = ordinaryUserRepository.existsByPhoneNumber(phone);
        if (exists) {
            return ResponseEntity.ok(ApiResponse.success(
                    new UsernameCheckResponse(false, "该手机号已被注册")));
        }
        return ResponseEntity.ok(ApiResponse.success(
                new UsernameCheckResponse(true, "手机号可用")));
    }

    /** 验证注册验证码（验证码错误时 success=false） */
    @PostMapping(value = "/verify-code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<Boolean>> verifyRegisterCode(
            @RequestParam("phone") String phone,
            @RequestParam("code") String code) {
        try {
            boolean valid = authService.verifyRegisterCode(phone, code);
            if (!valid) {
                return ResponseEntity.ok(ApiResponse.error("验证码错误或已过期"));
            }
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 提交注册（必须先通过短信验证码校验）
     * 供安卓端使用，替代未校验验证码的 /api/register
     */
    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<String>> registerWithCode(
            @RequestParam("userName") String userName,
            @RequestParam("account") String account,
            @RequestParam("password") String password,
            @RequestParam("confirm_password") String confirmPassword,
            @RequestParam("code") String code) {
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.ok(ApiResponse.error("两次输入的密码不一致"));
        }
        if (code == null || code.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("请输入验证码"));
        }
        try {
            boolean valid = authService.verifyRegisterCode(account.trim(), code.trim());
            if (!valid) {
                return ResponseEntity.ok(ApiResponse.error("验证码错误或已过期"));
            }
            String userId = authService.register(userName.trim(), account.trim(), password);
            return ResponseEntity.ok(ApiResponse.success(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
