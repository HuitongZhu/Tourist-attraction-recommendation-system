package com.travel.travelweb.web;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.service.UserProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        return userProfileService.getProfile(uid)
                .map(p -> {
                    model.addAttribute("profile", p);
                    model.addAttribute("navKey", "user");
                    return "user-profile";
                })
                .orElse("redirect:/login");
    }

    @GetMapping("/profile/edit")
    public String editForm(HttpSession session, Model model) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        return userProfileService.getProfile(uid)
                .map(p -> {
                    model.addAttribute("profile", p);
                    model.addAttribute("navKey", "user");
                    return "user-profile-edit";
                })
                .orElse("redirect:/login");
    }

    @PostMapping("/profile/edit")
    public String editSave(
            HttpSession session,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String idNumber,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String birthday,
            RedirectAttributes ra) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        userProfileService.updateProfile(uid, realName, phoneNumber, idNumber, gender, birthday);
        ra.addFlashAttribute("msg", "保存成功");
        return "redirect:/user/profile";
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("navKey", "user");
        return "user-password";
    }

    @PostMapping("/password")
    public String passwordSave(
            HttpSession session,
            @RequestParam(required = false) String verifyType,
            @RequestParam(required = false) String verifyValue,
            @RequestParam(required = false) String phone,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "两次输入的密码不一致");
            if (verifyType != null) {
                ra.addFlashAttribute("verifyType", verifyType);
            }
            return "redirect:/user/password";
        }
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        try {
            userProfileService.changePasswordWithVerify(uid, verifyType, verifyValue, newPassword);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            if (verifyType != null) {
                ra.addFlashAttribute("verifyType", verifyType);
            }
            return "redirect:/user/password";
        }
        ra.addFlashAttribute("msg", "密码已更新");
        return "redirect:/user/profile";
    }

    @PostMapping("/send-password-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendPasswordCode(@RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        try {
            String code = userProfileService.sendPasswordCode(phone);
            result.put("success", true);
            result.put("message", "验证码已发送");
            result.put("code", code);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-phone")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPhone(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        try {
            String phone = userProfileService.getPhone(uid);
            result.put("success", true);
            result.put("phone", phone);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/send-delete-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendDeleteCode(@RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        try {
            String code = userProfileService.sendDeleteCode(phone);
            result.put("success", true);
            result.put("message", "验证码已发送");
            result.put("code", code);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAccount(
            HttpSession session,
            @RequestParam(defaultValue = "password") String type,
            @RequestParam String value) {
        Map<String, Object> result = new HashMap<>();
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        try {
            boolean valid = userProfileService.verifyAndDelete(uid, type, value);
            if (!valid) {
                result.put("success", false);
                result.put("message", type.equals("password") ? "密码错误" : "验证码错误或已过期");
                return ResponseEntity.ok(result);
            }
            userProfileService.deleteAccount(uid);
            session.invalidate();
            result.put("success", true);
            result.put("message", "账号已成功注销");
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
