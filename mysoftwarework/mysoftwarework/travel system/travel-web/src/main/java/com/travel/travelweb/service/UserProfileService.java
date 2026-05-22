package com.travel.travelweb.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.travelweb.entity.OrdinaryUser;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import com.travel.travelweb.repo.SysUserRepository;

@Service
public class UserProfileService {

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    
    private final java.util.concurrent.ConcurrentHashMap<String, CodeInfo> deleteCodeCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, CodeInfo> passwordCodeCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    private final SysUserRepository sysUserRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final LandCollectRepository landCollectRepository;
    private final LandCommentRepository landCommentRepository;
    private final LandLikeRepository landLikeRepository;
    private final LandscapeRepository landscapeRepository;
    private final PostCommentRepository postCommentRepository;
    private final RecommendationPostRepository recommendationPostRepository;

    public UserProfileService(SysUserRepository sysUserRepository, 
                             OrdinaryUserRepository ordinaryUserRepository,
                             LandCollectRepository landCollectRepository,
                             LandCommentRepository landCommentRepository,
                             LandLikeRepository landLikeRepository,
                             LandscapeRepository landscapeRepository,
                             PostCommentRepository postCommentRepository,
                             RecommendationPostRepository recommendationPostRepository) {
        this.sysUserRepository = sysUserRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.landCollectRepository = landCollectRepository;
        this.landCommentRepository = landCommentRepository;
        this.landLikeRepository = landLikeRepository;
        this.landscapeRepository = landscapeRepository;
        this.postCommentRepository = postCommentRepository;
        this.recommendationPostRepository = recommendationPostRepository;
    }

    public record Profile(
            String userId,
            String userName,
            String realName,
            String phoneNumber,
            String idNumber,
            String gender,
            String birthday,
            java.time.LocalDateTime registerTime
    ) {
    }

    public Optional<Profile> getProfile(String userId) {
        Optional<SysUser> u = sysUserRepository.findById(userId);
        if (u.isEmpty()) {
            return Optional.empty();
        }
        OrdinaryUser ou = ordinaryUserRepository.findById(userId).orElse(null);
        return Optional.of(new Profile(
                u.get().getUserId(),
                u.get().getUserName(),
                ou != null ? ou.getRealName() : null,
                ou != null ? ou.getPhoneNumber() : null,
                ou != null ? ou.getIdNumber() : null,
                ou != null ? ou.getGender() : null,
                ou != null ? ou.getBirthday() : null,
                ou != null ? ou.getRegisterTime() : null
        ));
    }

    @Transactional
    public void updateProfile(String userId, String realName, String phone, String idNumber, String gender, String birthday) {
        OrdinaryUser ou = ordinaryUserRepository.findById(userId).orElseGet(() -> {
            OrdinaryUser n = new OrdinaryUser();
            n.setUserId(userId);
            n.setRegisterTime(java.time.LocalDateTime.now());
            return n;
        });
        ou.setRealName(emptyToNull(realName));
        ou.setPhoneNumber(emptyToNull(phone));
        ou.setIdNumber(emptyToNull(idNumber));
        ou.setGender(emptyToNull(gender));
        ou.setBirthday(emptyToNull(birthday));
        ordinaryUserRepository.save(ou);
    }

    @Transactional
    public void changePassword(String userId, String oldPwd, String newPwd) {
        SysUser u = sysUserRepository.findById(userId).orElseThrow();
        if (!oldPwd.equals(u.getUserPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        if (newPwd == null || newPwd.length() < 6 || newPwd.length() > 30) {
            throw new IllegalArgumentException("新密码长度应为6-30位");
        }
        u.setUserPassword(newPwd);
        sysUserRepository.save(u);
    }

    @Transactional
    public void changePasswordWithVerify(String userId, String verifyType, String verifyValue, String newPwd) {
        System.out.println("========== 修改密码验证开始 ==========");
        System.out.println("用户ID: " + userId);
        System.out.println("验证类型: " + verifyType);
        System.out.println("验证值: [" + verifyValue + "]");
        System.out.println("新密码: " + (newPwd != null ? "[已提供，长度：" + newPwd.length() + "]" : "null"));
        
        if (newPwd == null || newPwd.trim().length() < 6 || newPwd.trim().length() > 30) {
            throw new IllegalArgumentException("新密码长度应为6-30位");
        }

        if ("password".equals(verifyType)) {
            SysUser u = sysUserRepository.findById(userId).orElseThrow();
            System.out.println("数据库密码: [" + u.getUserPassword() + "]");
            if (verifyValue == null || verifyValue.trim().isEmpty()) {
                System.out.println("验证值不能为空");
                throw new IllegalArgumentException("请输入原密码");
            }
            if (!verifyValue.trim().equals(u.getUserPassword())) {
                System.out.println("密码不匹配，输入值和数据库值不同");
                throw new IllegalArgumentException("原密码不正确");
            }
            u.setUserPassword(newPwd.trim());
            sysUserRepository.save(u);
            System.out.println("密码验证通过，修改成功");
        } else if ("sms".equals(verifyType)) {
            OrdinaryUser ou = ordinaryUserRepository.findById(userId).orElse(null);
            if (ou == null || ou.getPhoneNumber() == null) {
                System.out.println("用户未绑定手机号");
                throw new IllegalArgumentException("未绑定手机号");
            }
            String phone = ou.getPhoneNumber();
            System.out.println("用户手机号: " + phone);
            CodeInfo codeInfo = passwordCodeCache.get(phone);
            
            if (codeInfo == null) {
                System.out.println("验证码未找到，可能未发送或已过期");
                throw new IllegalArgumentException("请先获取验证码");
            }
            
            if (codeInfo.expireTime.isBefore(java.time.LocalDateTime.now())) {
                passwordCodeCache.remove(phone);
                System.out.println("验证码已过期");
                throw new IllegalArgumentException("验证码已过期，请重新获取");
            }
            
            System.out.println("输入验证码: [" + verifyValue + "], 数据库验证码: [" + codeInfo.code + "]");
            if (verifyValue == null || verifyValue.trim().isEmpty()) {
                System.out.println("验证码输入为空");
                throw new IllegalArgumentException("请输入验证码");
            }
            if (!verifyValue.trim().equals(codeInfo.code)) {
                System.out.println("验证码不匹配");
                throw new IllegalArgumentException("验证码不正确");
            }
            
            passwordCodeCache.remove(phone);
            
            SysUser u = sysUserRepository.findById(userId).orElseThrow();
            u.setUserPassword(newPwd.trim());
            sysUserRepository.save(u);
            System.out.println("验证码验证通过，修改成功");
        } else {
            System.out.println("未知的验证类型: " + verifyType);
            throw new IllegalArgumentException("请选择验证方式");
        }
        System.out.println("==================================");
    }

    public String sendPasswordCode(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String phone = phoneNumber.trim();
        if (!phone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("请输入正确的手机号格式");
        }
        
        CodeInfo existing = passwordCodeCache.get(phone);
        if (existing != null && existing.sendTime.plusSeconds(60).isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("验证码发送过于频繁，请稍后再试");
        }
        
        String code = generateCode();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        passwordCodeCache.put(phone, new CodeInfo(code, now, now.plusMinutes(CODE_EXPIRE_MINUTES)));
        
        System.out.println("========== 修改密码验证码 ==========");
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("有效期: " + CODE_EXPIRE_MINUTES + "分钟");
        System.out.println("==================================");
        return code;
    }

    @Transactional
    public void deleteAccount(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        landCollectRepository.deleteByUserId(userId);
        landCommentRepository.deleteByUserId(userId);
        landLikeRepository.deleteByUserId(userId);
        landscapeRepository.deleteByUserId(userId);
        postCommentRepository.deleteByUserId(userId);
        recommendationPostRepository.deleteByUserId(userId);
        ordinaryUserRepository.deleteByUserId(userId);
        sysUserRepository.deleteById(userId);
    }

    public String getPhone(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        OrdinaryUser ou = ordinaryUserRepository.findById(userId).orElse(null);
        if (ou == null || ou.getPhoneNumber() == null || ou.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("未绑定手机号");
        }
        return ou.getPhoneNumber();
    }

    public String sendDeleteCode(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String phone = phoneNumber.trim();
        if (!phone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("请输入正确的手机号格式");
        }
        
        CodeInfo existing = deleteCodeCache.get(phone);
        if (existing != null && existing.sendTime.plusSeconds(60).isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("验证码发送过于频繁，请稍后再试");
        }
        
        String code = generateCode();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        deleteCodeCache.put(phone, new CodeInfo(code, now, now.plusMinutes(CODE_EXPIRE_MINUTES)));
        
        System.out.println("========== 注销验证码 ==========");
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("有效期: " + CODE_EXPIRE_MINUTES + "分钟");
        System.out.println("=================================");
        return code;
    }

    public boolean verifyAndDelete(String userId, String type, String value) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        if ("password".equals(type)) {
            SysUser u = sysUserRepository.findById(userId).orElse(null);
            return u != null && value.equals(u.getUserPassword());
        } else if ("sms".equals(type)) {
            OrdinaryUser ou = ordinaryUserRepository.findById(userId).orElse(null);
            if (ou == null || ou.getPhoneNumber() == null) {
                return false;
            }
            String phone = ou.getPhoneNumber();
            CodeInfo codeInfo = deleteCodeCache.get(phone);
            
            if (codeInfo == null) {
                return false;
            }
            
            if (codeInfo.expireTime.isBefore(java.time.LocalDateTime.now())) {
                deleteCodeCache.remove(phone);
                return false;
            }
            
            boolean valid = value.equals(codeInfo.code);
            if (valid) {
                deleteCodeCache.remove(phone);
            }
            return valid;
        }
        
        return false;
    }
    
    private String generateCode() {
        java.util.Random random = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    private static class CodeInfo {
        String code;
        java.time.LocalDateTime sendTime;
        java.time.LocalDateTime expireTime;

        CodeInfo(String code, java.time.LocalDateTime sendTime, java.time.LocalDateTime expireTime) {
            this.code = code;
            this.sendTime = sendTime;
            this.expireTime = expireTime;
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
