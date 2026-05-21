package com.travel.travelweb.web;

import com.travel.travelweb.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    private final AuthService authService;

    public ForgotPasswordController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("step")) {
            model.addAttribute("step", 1);
        }
        return "forgot-password";
    }

    @PostMapping("/verify-phone")
    public String verifyPhone(@RequestParam String phone, Model model, RedirectAttributes ra) {
        try {
            authService.validatePhoneForReset(phone);
            String phoneSuffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;
            model.addAttribute("step", 2);
            model.addAttribute("phone", phone);
            model.addAttribute("phoneSuffix", phoneSuffix);
            return "forgot-password";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("step", 1);
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/send-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendCode(@RequestParam String phone) {
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

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String phone, @RequestParam String code, Model model, RedirectAttributes ra) {
        try {
            boolean valid = authService.verifyResetPasswordCodeByPhone(phone, code);
            if (!valid) {
                ra.addFlashAttribute("error", "验证码错误或已过期");
                ra.addFlashAttribute("step", 2);
                ra.addFlashAttribute("phone", phone);
                ra.addFlashAttribute("phoneSuffix", phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone);
                return "redirect:/forgot-password";
            }
            model.addAttribute("step", 3);
            model.addAttribute("phone", phone);
            return "forgot-password";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("step", 2);
            ra.addFlashAttribute("phone", phone);
            try {
                ra.addFlashAttribute("phoneSuffix", phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone);
            } catch (Exception ignored) {}
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String phone, @RequestParam String newPassword, 
                                @RequestParam String confirmPassword, Model model, RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "两次输入的密码不一致");
            ra.addFlashAttribute("step", 3);
            ra.addFlashAttribute("phone", phone);
            return "redirect:/forgot-password";
        }
        
        try {
            authService.resetPasswordByPhone(phone, newPassword);
            model.addAttribute("step", 4);
            return "forgot-password";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("step", 3);
            ra.addFlashAttribute("phone", phone);
            return "redirect:/forgot-password";
        }
    }
}