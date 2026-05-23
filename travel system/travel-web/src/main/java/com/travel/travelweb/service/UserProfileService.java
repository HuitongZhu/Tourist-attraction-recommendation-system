package com.travel.travelweb.service;

import com.travel.travelweb.entity.OrdinaryUser;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserProfileService {

    private final SysUserRepository sysUserRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final LandCollectRepository landCollectRepository;
    private final LandCommentRepository landCommentRepository;
    private final LandLikeRepository landLikeRepository;
    private final LandscapeRepository landscapeRepository;
    private final PostCommentRepository postCommentRepository;
    private final RecommendationPostRepository recommendationPostRepository;
    private final PasswordEncoder passwordEncoder;
    private final AliyunSmsService aliyunSmsService;
    private final SmsCodeCacheService smsCodeCacheService;

    public UserProfileService(SysUserRepository sysUserRepository, 
                             OrdinaryUserRepository ordinaryUserRepository,
                             LandCollectRepository landCollectRepository,
                             LandCommentRepository landCommentRepository,
                             LandLikeRepository landLikeRepository,
                             LandscapeRepository landscapeRepository,
                             PostCommentRepository postCommentRepository,
                             RecommendationPostRepository recommendationPostRepository,
                             PasswordEncoder passwordEncoder,
                             AliyunSmsService aliyunSmsService,
                             SmsCodeCacheService smsCodeCacheService) {
        this.sysUserRepository = sysUserRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.landCollectRepository = landCollectRepository;
        this.landCommentRepository = landCommentRepository;
        this.landLikeRepository = landLikeRepository;
        this.landscapeRepository = landscapeRepository;
        this.postCommentRepository = postCommentRepository;
        this.recommendationPostRepository = recommendationPostRepository;
        this.passwordEncoder = passwordEncoder;
        this.aliyunSmsService = aliyunSmsService;
        this.smsCodeCacheService = smsCodeCacheService;
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
        if (!passwordEncoder.matches(oldPwd, u.getUserPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        if (newPwd == null || newPwd.length() < 6 || newPwd.length() > 30) {
            throw new IllegalArgumentException("新密码长度应为6-30位");
        }
        if (passwordEncoder.matches(newPwd, u.getUserPassword())) {
            throw new IllegalArgumentException("新密码不能与旧密码相同，请重新设置新密码");
        }
        u.setUserPassword(passwordEncoder.encode(newPwd));
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

        SysUser u = sysUserRepository.findById(userId).orElseThrow();
        if (passwordEncoder.matches(newPwd.trim(), u.getUserPassword())) {
            throw new IllegalArgumentException("新密码不能与旧密码相同，请重新设置新密码");
        }

        if ("password".equals(verifyType)) {
            System.out.println("数据库密码: [" + u.getUserPassword() + "]");
            if (verifyValue == null || verifyValue.trim().isEmpty()) {
                System.out.println("验证值不能为空");
                throw new IllegalArgumentException("请输入原密码");
            }
            if (!passwordEncoder.matches(verifyValue.trim(), u.getUserPassword())) {
                System.out.println("密码不匹配，输入值和数据库值不同");
                throw new IllegalArgumentException("原密码不正确");
            }
            u.setUserPassword(passwordEncoder.encode(newPwd.trim()));
            sysUserRepository.save(u);
            System.out.println("密码验证通过，修改成功");
        } else if ("sms".equals(verifyType)) {
            OrdinaryUser ou = ordinaryUserRepository.findById(userId).orElse(null);
            if (ou == null || ou.getPhoneNumber() == null) {
                throw new IllegalArgumentException("未绑定手机号");
            }
            String phone = ou.getPhoneNumber();
            if (verifyValue == null || verifyValue.trim().isEmpty()) {
                throw new IllegalArgumentException("请输入验证码");
            }
            if (!verifySmsCode(phone, verifyValue.trim(), SmsCodeType.PASSWORD)) {
                throw new IllegalArgumentException("验证码不正确或已过期");
            }
            u.setUserPassword(passwordEncoder.encode(newPwd.trim()));
            sysUserRepository.save(u);
        } else {
            System.out.println("未知的验证类型: " + verifyType);
            throw new IllegalArgumentException("请选择验证方式");
        }
        System.out.println("==================================");
    }

    public void sendPasswordCode(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String phone = phoneNumber.trim();
        if (!phone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("请输入正确的手机号格式");
        }
        sendAliyunCode(phone, SmsCodeType.PASSWORD);
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

    public void sendDeleteCode(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String phone = phoneNumber.trim();
        if (!phone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("请输入正确的手机号格式");
        }
        sendAliyunCode(phone, SmsCodeType.DELETE);
    }

    public boolean verifyAndDelete(String userId, String type, String value) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        if ("password".equals(type)) {
            SysUser u = sysUserRepository.findById(userId).orElse(null);
            return u != null && passwordEncoder.matches(value, u.getUserPassword());
        } else if ("sms".equals(type)) {
            OrdinaryUser ou = ordinaryUserRepository.findById(userId).orElse(null);
            if (ou == null || ou.getPhoneNumber() == null) {
                return false;
            }
            return verifySmsCode(ou.getPhoneNumber(), value, SmsCodeType.DELETE);
        }
        
        return false;
    }

    private void sendAliyunCode(String phone, SmsCodeType type) {
        smsCodeCacheService.checkSendInterval(phone, type);
        String outId = aliyunSmsService.sendVerifyCode(phone);
        smsCodeCacheService.saveSession(phone, type, outId);
    }

    private boolean verifySmsCode(String phone, String code, SmsCodeType type) {
        String outId = smsCodeCacheService.getOutId(phone, type);
        if (outId == null) {
            return false;
        }
        boolean passed = aliyunSmsService.checkVerifyCode(phone, code, outId);
        if (passed) {
            smsCodeCacheService.removeSession(phone, type);
        }
        return passed;
    }

    private static String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
