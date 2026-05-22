package com.travel.travelweb.api.dto;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.entity.OrdinaryUser;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.service.UserService;

/**
 * 管理员编辑用户资料（供安卓端）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final SysUserRepository sysUserRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final UserService userService;

    public AdminUserController(
            SysUserRepository sysUserRepository,
            OrdinaryUserRepository ordinaryUserRepository,
            UserService userService) {
        this.sysUserRepository = sysUserRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.userService = userService;
    }

    /** 用户完整资料（编辑弹窗用） */
    @GetMapping("/users/{id}/detail")
    public ResponseEntity<ApiResponse<UserResponse>> userDetail(@PathVariable String id) {
        return sysUserRepository.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success(toUserResponse(user))))
                .orElse(ResponseEntity.ok(ApiResponse.error("用户不存在")));
    }

    /** 保存用户资料 */
    @PutMapping("/users/{id}/profile")
    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable String id,
            @RequestBody AdminUpdateUserRequest request) {
        SysUser user = sysUserRepository.findById(id)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        }
        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            userService.updateUserName(id, request.getUserName().trim());
            user = sysUserRepository.findById(id).orElse(user);
        }
        OrdinaryUser ou = ordinaryUserRepository.findById(id).orElseGet(() -> {
            OrdinaryUser n = new OrdinaryUser();
            n.setUserId(id);
            return n;
        });
        if (request.getRealName() != null) {
            ou.setRealName(request.getRealName().trim());
        }
        if (request.getPhoneNumber() != null) {
            ou.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getIdNumber() != null) {
            ou.setIdNumber(request.getIdNumber().trim());
        }
        if (request.getGender() != null) {
            ou.setGender(request.getGender().trim());
        }
        if (request.getBirthday() != null) {
            ou.setBirthday(request.getBirthday().trim());
        }
        ordinaryUserRepository.save(ou);
        return ResponseEntity.ok(ApiResponse.success(toUserResponse(user)));
    }

    private UserResponse toUserResponse(SysUser user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setUserType(user.getUserType());
        ordinaryUserRepository.findById(user.getUserId()).ifPresent(ou -> {
            response.setPhoneNumber(ou.getPhoneNumber());
            response.setRealName(ou.getRealName());
            response.setIdNumber(ou.getIdNumber());
            response.setGender(ou.getGender());
            response.setBirthday(ou.getBirthday());
        });
        return response;
    }
}
