package com.travel.travelweb.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.service.AuthService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "next", required = false) String next, 
                           @RequestParam(value = "account", required = false) String account,
                           @RequestParam(value = "userType", required = false) String userType,
                           @RequestParam(value = "loginType", required = false) String loginType,
                           Model model) {
        model.addAttribute("next", next != null ? next : "/");
        model.addAttribute("navKey", "");
        if (account != null && !account.isBlank()) {
            model.addAttribute("account", account);
        }
        if (userType != null && !userType.isBlank()) {
            model.addAttribute("userType", userType);
        }
        if (loginType != null && !loginType.isBlank()) {
            model.addAttribute("loginType", loginType);
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String account,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "2") String userType,
            @RequestParam(defaultValue = "password") String loginType,
            @RequestParam(value = "next", required = false, defaultValue = "/") String next,
            HttpSession session,
            RedirectAttributes ra) {
        
        SysUser user;
        
        if ("sms".equals(loginType)) {
            Optional<SysUser> u = authService.loginBySmsCode(account, code, userType);
            if (u.isEmpty()) {
                // 检查是否是身份不匹配
                String phone = account.trim();
                boolean isAdmin = "1".equals(userType);
                boolean isPhoneInAdmin = authService.checkPhoneInAdmin(phone);
                boolean isPhoneInOrdinary = authService.checkPhoneInOrdinary(phone);
                
                String errorMsg = "验证码错误或已过期";
                if (isAdmin && isPhoneInOrdinary) {
                    errorMsg = "所选身份与账号不匹配";
                } else if (!isAdmin && isPhoneInAdmin) {
                    errorMsg = "所选身份与账号不匹配";
                }
                
                ra.addFlashAttribute("error", errorMsg);
                return "redirect:/login?next=" + java.net.URLEncoder.encode(next, java.nio.charset.StandardCharsets.UTF_8) 
                    + "&account=" + java.net.URLEncoder.encode(account, java.nio.charset.StandardCharsets.UTF_8)
                    + "&userType=" + java.net.URLEncoder.encode(userType, java.nio.charset.StandardCharsets.UTF_8)
                    + "&loginType=" + java.net.URLEncoder.encode(loginType, java.nio.charset.StandardCharsets.UTF_8);
            }
            user = u.get();
        } else {
            com.travel.travelweb.service.LoginResult result = authService.login(account, password, userType);
            if (!result.isSuccess()) {
                ra.addFlashAttribute("error", result.getErrorMessage());
                return "redirect:/login?next=" + java.net.URLEncoder.encode(next, java.nio.charset.StandardCharsets.UTF_8) 
                    + "&account=" + java.net.URLEncoder.encode(account, java.nio.charset.StandardCharsets.UTF_8)
                    + "&userType=" + java.net.URLEncoder.encode(userType, java.nio.charset.StandardCharsets.UTF_8)
                    + "&loginType=" + java.net.URLEncoder.encode(loginType, java.nio.charset.StandardCharsets.UTF_8);
            }
            user = result.getUser().get();
        }
        
        session.setAttribute(LoginInterceptor.SESSION_USER_ID, user.getUserId());
        session.setAttribute(LoginInterceptor.SESSION_USER_NAME, user.getUserName());
        session.setAttribute(LoginInterceptor.SESSION_USER_TYPE, user.getUserType());
        
        String redirectUrl;
        if ("1".equals(user.getUserType())) {
            redirectUrl = "/admin";
        } else {
            redirectUrl = "/";
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/send-sms-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendSmsCode(@RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        try {
            String code = authService.sendSmsCode(phone);
            result.put("success", true);
            result.put("message", "验证码已发送");
            result.put("code", code);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("step")) {
            model.addAttribute("step", 1);
        }
        return "register";
    }

    @PostMapping("/register/verify-account")
    public String verifyAccount(
            @RequestParam String userName,
            @RequestParam String password,
            @RequestParam String confirm_password,
            Model model,
            RedirectAttributes ra) {
        if (!password.equals(confirm_password)) {
            ra.addFlashAttribute("error", "两次输入的密码不一致");
            ra.addFlashAttribute("step", 1);
            return "redirect:/register";
        }
        if (password.length() < 6 || password.length() > 30) {
            ra.addFlashAttribute("error", "密码长度应为6-30位");
            ra.addFlashAttribute("step", 1);
            return "redirect:/register";
        }
        try {
            authService.validateUserName(userName);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("step", 1);
            return "redirect:/register";
        }
        model.addAttribute("step", 2);
        model.addAttribute("userName", userName);
        model.addAttribute("password", password);
        return "register";
    }

    @PostMapping("/register/send-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendRegisterCode(@RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        try {
            String code = authService.sendRegisterSmsCode(phone);
            result.put("success", true);
            result.put("message", "验证码已发送");
            result.put("code", code);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register/verify-code")
    public String verifyCode(
            @RequestParam String userName,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam String code,
            Model model,
            RedirectAttributes ra) {
        try {
            boolean valid = authService.verifyRegisterCode(phone, code);
            if (!valid) {
                ra.addFlashAttribute("error", "验证码错误或已过期");
                ra.addFlashAttribute("step", 2);
                ra.addFlashAttribute("userName", userName);
                ra.addFlashAttribute("password", password);
                ra.addFlashAttribute("phone", phone);
                return "redirect:/register";
            }
            authService.register(userName.trim(), phone.trim(), password);
            model.addAttribute("step", 3);
            return "register";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("step", 2);
            ra.addFlashAttribute("userName", userName);
            ra.addFlashAttribute("password", password);
            ra.addFlashAttribute("phone", phone);
            return "redirect:/register";
        }
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String userName,
            @RequestParam String account,
            @RequestParam String password,
            @RequestParam String confirm_password,
            RedirectAttributes ra) {
        if (!password.equals(confirm_password)) {
            ra.addFlashAttribute("error", "两次输入的密码不一致");
            return "redirect:/register";
        }
        try {
            authService.register(userName.trim(), account.trim(), password);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
        ra.addFlashAttribute("msg", "注册成功，请登录");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}