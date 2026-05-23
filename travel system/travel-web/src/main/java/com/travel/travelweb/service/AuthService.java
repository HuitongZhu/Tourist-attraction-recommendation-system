package com.travel.travelweb.service;

import com.travel.travelweb.entity.Administrator;
import com.travel.travelweb.entity.OrdinaryUser;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.AdministratorRepository;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.util.IdGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    public static final String USER_TYPE_ADMIN = "1";
    public static final String USER_TYPE_ORDINARY = "2";

    private final SysUserRepository userRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AliyunSmsService aliyunSmsService;
    private final SmsCodeCacheService smsCodeCacheService;

    public AuthService(SysUserRepository userRepository,
                       OrdinaryUserRepository ordinaryUserRepository,
                       AdministratorRepository administratorRepository,
                       PasswordEncoder passwordEncoder,
                       AliyunSmsService aliyunSmsService,
                       SmsCodeCacheService smsCodeCacheService) {
        this.userRepository = userRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
        this.aliyunSmsService = aliyunSmsService;
        this.smsCodeCacheService = smsCodeCacheService;
    }

    public LoginResult login(String account, String password, String userType) {
        if (account == null || account.isBlank() || password == null) {
            return LoginResult.accountNotFound("账号或密码不能为空");
        }
        String a = account.trim();
        Optional<SysUser> u = userRepository.findByUserNameAndUserType(a, userType);
        if (u.isEmpty()) {
            u = userRepository.findByUserIdAndUserType(a, userType);
        }
        if (u.isEmpty()) {
            if (USER_TYPE_ORDINARY.equals(userType)) {
                Optional<Administrator> adminByPhone = administratorRepository.findByPhoneNumber(a);
                if (adminByPhone.isPresent()) {
                    return LoginResult.accountNotFound("所选身份与账号不匹配");
                }
                Optional<SysUser> sysUserByName = userRepository.findByUserName(a);
                if (sysUserByName.isPresent() && USER_TYPE_ADMIN.equals(sysUserByName.get().getUserType())) {
                    return LoginResult.accountNotFound("所选身份与账号不匹配");
                }
                Optional<OrdinaryUser> ou = ordinaryUserRepository.findByPhoneNumber(a);
                if (ou.isPresent()) {
                    u = userRepository.findById(ou.get().getUserId());
                }
            } else if (USER_TYPE_ADMIN.equals(userType)) {
                Optional<Administrator> admin = administratorRepository.findByPhoneNumber(a);
                if (admin.isEmpty()) {
                    if (ordinaryUserRepository.findByPhoneNumber(a).isPresent()) {
                        return LoginResult.accountNotFound("所选身份与账号不匹配");
                    }
                    Optional<SysUser> userByName = userRepository.findByUserName(a);
                    if (userByName.isPresent()) {
                        return LoginResult.accountNotFound("所选身份与账号不匹配");
                    }
                } else {
                    u = userRepository.findById(admin.get().getUserId());
                }
            }
        }
        if (u.isEmpty()) {
            return LoginResult.accountNotFound("账号/手机号不存在");
        }

        String storedPassword = u.get().getUserPassword();
        if (passwordEncoder.matches(password, storedPassword)) {
            return LoginResult.success(u.get());
        }

        if (password.equals(storedPassword)) {
            SysUser user = u.get();
            user.setUserPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return LoginResult.success(user);
        }

        return LoginResult.wrongPassword("密码错误");
    }

    public Optional<SysUser> loginBySmsCode(String phoneNumber, String code, String userType) {
        if (phoneNumber == null || phoneNumber.isBlank() || code == null || code.isBlank()) {
            return Optional.empty();
        }

        String phone = phoneNumber.trim();
        if (!verifySmsCode(phone, code)) {
            return Optional.empty();
        }

        if (USER_TYPE_ADMIN.equals(userType)) {
            Optional<Administrator> admin = administratorRepository.findByPhoneNumber(phone);
            if (admin.isEmpty()) {
                return Optional.empty();
            }
            return userRepository.findById(admin.get().getUserId())
                    .filter(u -> USER_TYPE_ADMIN.equals(u.getUserType()));
        }

        Optional<Administrator> admin = administratorRepository.findByPhoneNumber(phone);
        if (admin.isPresent()) {
            return Optional.empty();
        }
        Optional<OrdinaryUser> ou = ordinaryUserRepository.findByPhoneNumber(phone);
        if (ou.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findById(ou.get().getUserId())
                .filter(u -> USER_TYPE_ORDINARY.equals(u.getUserType()));
    }

    public void sendSmsCode(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }

        String phone = phoneNumber.trim();
        validatePhoneFormat(phone);

        boolean exists = ordinaryUserRepository.existsByPhoneNumber(phone)
                || administratorRepository.existsByPhoneNumber(phone);
        if (!exists) {
            throw new IllegalArgumentException("该手机号未注册");
        }

        sendAliyunCode(phone);
    }

    public void validateUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        String name = userName.trim();
        if (userRepository.existsByUserName(name)) {
            throw new IllegalArgumentException("用户名已被占用");
        }
    }

    public void sendRegisterSmsCode(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }

        String phone = phoneNumber.trim();
        validatePhoneFormat(phone);

        if (ordinaryUserRepository.existsByPhoneNumber(phone)) {
            throw new IllegalArgumentException("该手机号已被注册");
        }

        sendAliyunCode(phone);
    }

    public boolean verifyRegisterCode(String phoneNumber, String code) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        return verifySmsCode(phoneNumber.trim(), code);
    }

    public String getPhoneByAccount(String account) {
        if (account == null || account.isBlank()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        String a = account.trim();

        Optional<SysUser> sysUser = userRepository.findByUserNameAndUserType(a, USER_TYPE_ORDINARY);
        if (sysUser.isEmpty()) {
            sysUser = userRepository.findByUserIdAndUserType(a, USER_TYPE_ORDINARY);
        }

        if (sysUser.isEmpty()) {
            throw new IllegalArgumentException("账号不存在");
        }

        Optional<OrdinaryUser> ou = ordinaryUserRepository.findByUserId(sysUser.get().getUserId());
        if (ou.isEmpty() || ou.get().getPhoneNumber() == null || ou.get().getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("该账号未绑定手机号");
        }

        return ou.get().getPhoneNumber();
    }

    public String getPhoneSuffixByAccount(String account) {
        String phone = getPhoneByAccount(account);
        if (phone.length() >= 4) {
            return phone.substring(phone.length() - 4);
        }
        return phone;
    }

    public boolean verifyResetPasswordCode(String account, String code) {
        String phone = getPhoneByAccount(account);
        return verifySmsCode(phone, code);
    }

    @Transactional
    public void resetPassword(String account, String newPassword) {
        if (account == null || account.isBlank()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 30) {
            throw new IllegalArgumentException("密码长度应为6-30位");
        }

        String a = account.trim();
        Optional<SysUser> sysUser = userRepository.findByUserNameAndUserType(a, USER_TYPE_ORDINARY);
        if (sysUser.isEmpty()) {
            sysUser = userRepository.findByUserIdAndUserType(a, USER_TYPE_ORDINARY);
        }

        if (sysUser.isEmpty()) {
            throw new IllegalArgumentException("账号不存在");
        }

        SysUser user = sysUser.get();
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void validatePhoneForReset(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String phone = phoneNumber.trim();
        validatePhoneFormat(phone);
        if (!ordinaryUserRepository.existsByPhoneNumber(phone)) {
            throw new IllegalArgumentException("该手机号未注册");
        }
    }

    public boolean checkPhoneInAdmin(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        return administratorRepository.findByPhoneNumber(phoneNumber.trim()).isPresent();
    }

    public boolean checkPhoneInOrdinary(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        return ordinaryUserRepository.findByPhoneNumber(phoneNumber.trim()).isPresent();
    }

    public boolean verifyResetPasswordCodeByPhone(String phoneNumber, String code) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        return verifySmsCode(phoneNumber.trim(), code);
    }

    @Transactional
    public void resetPasswordByPhone(String phoneNumber, String newPassword) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 30) {
            throw new IllegalArgumentException("密码长度应为6-30位");
        }

        String phone = phoneNumber.trim();
        Optional<OrdinaryUser> ou = ordinaryUserRepository.findByPhoneNumber(phone);
        if (ou.isEmpty()) {
            throw new IllegalArgumentException("该手机号未注册");
        }

        Optional<SysUser> sysUser = userRepository.findById(ou.get().getUserId());
        if (sysUser.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }

        SysUser user = sysUser.get();
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public String register(String userName, String phone, String password) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.length() < 6 || password.length() > 30) {
            throw new IllegalArgumentException("密码长度应为6-30位");
        }
        if (phone != null && !phone.isBlank()) {
            String p = phone.trim();
            if (!p.matches("^1\\d{10}$")) {
                throw new IllegalArgumentException("请输入正确的手机号格式");
            }
            if (ordinaryUserRepository.existsByPhoneNumber(p)) {
                throw new IllegalArgumentException("该手机号已被注册");
            }
        }
        String name = userName.trim();
        if (userRepository.existsByUserName(name)) {
            throw new IllegalArgumentException("用户名已被占用");
        }
        String userId = IdGenerator.next("U");
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName(name);
        user.setUserPassword(passwordEncoder.encode(password));
        user.setUserType(USER_TYPE_ORDINARY);
        userRepository.save(user);

        OrdinaryUser ou = new OrdinaryUser();
        ou.setUserId(userId);
        ou.setPhoneNumber(phone != null ? phone.trim() : null);
        ou.setRegisterTime(LocalDateTime.now());
        ordinaryUserRepository.save(ou);
        return userId;
    }

    private void sendAliyunCode(String phone) {
        smsCodeCacheService.checkSendInterval(phone, SmsCodeType.AUTH);

        String outId = aliyunSmsService.sendVerifyCode(phone);
        smsCodeCacheService.saveSession(phone, SmsCodeType.AUTH, outId);
    }

    private boolean verifySmsCode(String phone, String code) {
        String outId = smsCodeCacheService.getOutId(phone, SmsCodeType.AUTH);
        if (outId == null) {
            return false;
        }
        boolean passed = aliyunSmsService.checkVerifyCode(phone, code.trim(), outId);
        if (passed) {
            smsCodeCacheService.removeSession(phone, SmsCodeType.AUTH);
        }
        return passed;
    }

    private static void validatePhoneFormat(String phone) {
        if (!phone.matches("^1\\d{10,11}$")) {
            throw new IllegalArgumentException("请输入正确的手机号格式");
        }
    }
}
