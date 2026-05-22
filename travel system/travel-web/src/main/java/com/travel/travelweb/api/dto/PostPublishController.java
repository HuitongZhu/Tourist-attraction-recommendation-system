package com.travel.travelweb.api.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.entity.LandComment;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.PostComment;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.service.AuthService;
import com.travel.travelweb.service.LandCommentService;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostService;
import com.travel.travelweb.service.UserProfileService;

/**
 * 推荐帖发布相关 API（供安卓端使用）
 */
@RestController
@RequestMapping("/api")
public class PostPublishController {

    private final PostService postService;
    private final LandscapeService landscapeService;
    private final LandscapeRepository landscapeRepository;
    private final RecommendationPostRepository postRepository;
    private final LandLikeRepository landLikeRepository;
    private final LandCommentService landCommentService;
    private final LandCommentRepository landCommentRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserProfileService userProfileService;
    private final SysUserRepository sysUserRepository;
    private final AuthService authService;

    public PostPublishController(
            PostService postService,
            LandscapeService landscapeService,
            LandscapeRepository landscapeRepository,
            RecommendationPostRepository postRepository,
            LandLikeRepository landLikeRepository,
            LandCommentService landCommentService,
            LandCommentRepository landCommentRepository,
            PostCommentRepository postCommentRepository,
            UserProfileService userProfileService,
            SysUserRepository sysUserRepository,
            AuthService authService) {
        this.postService = postService;
        this.landscapeService = landscapeService;
        this.landscapeRepository = landscapeRepository;
        this.postRepository = postRepository;
        this.landLikeRepository = landLikeRepository;
        this.landCommentService = landCommentService;
        this.landCommentRepository = landCommentRepository;
        this.postCommentRepository = postCommentRepository;
        this.userProfileService = userProfileService;
        this.sysUserRepository = sysUserRepository;
        this.authService = authService;
    }

    /** 发送验证码并返回验证码（弹窗展示）type: login|register|password|delete|forgot */
    @PostMapping(value = "/sms-send-code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<SmsCodeResponse>> sendSmsCode(
            @RequestParam("phone") String phone,
            @RequestParam(value = "type", defaultValue = "login") String type) {
        try {
            SmsCodeResponse data = SmsSendSupport.send(authService, userProfileService, phone, type);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /** 个人资料查看（与发布推荐帖同控制器，确保安卓端可访问） */
    @GetMapping("/user-profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        return userProfileService.getProfile(userId)
                .map(p -> ResponseEntity.ok(ApiResponse.success(toProfileResponse(p))))
                .orElse(ResponseEntity.ok(ApiResponse.error("用户不存在")));
    }

    /** 个人资料保存（含手机号） */
    @PutMapping("/user-profile")
    public ResponseEntity<ApiResponse<UserResponse>> saveUserProfile(
            @RequestBody UpdateProfileRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        if (request == null || isProfileAllBlank(request)) {
            return ResponseEntity.ok(ApiResponse.error("请至少填写一项要修改的内容"));
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            String phone = request.getPhoneNumber().trim();
            if (!phone.matches("^1\\d{10}$")) {
                return ResponseEntity.ok(ApiResponse.error("请输入正确的手机号"));
            }
        }
        try {
            userProfileService.updateProfile(
                    userId,
                    request.getRealName(),
                    request.getPhoneNumber(),
                    request.getIdNumber(),
                    request.getGender(),
                    request.getBirthday());
            return getUserProfile(userId);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /** 修改密码（原密码校验） */
    @PostMapping(value = "/user-change-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        try {
            userProfileService.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /** 短信验证码修改密码 */
    @PostMapping(value = "/user-reset-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<Void>> resetPasswordBySms(
            @RequestParam("phone") String phone,
            @RequestParam("code") String code,
            @RequestParam("newPassword") String newPassword,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        try {
            userProfileService.changePasswordWithVerify(userId, "sms", code, newPassword);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /** 修改本人评论（备用路径） */
    @PutMapping("/user-comment/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateUserComment(
            @PathVariable String id,
            @RequestBody CommentUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("评论内容不能为空"));
        }
        String content = request.getContent().trim();
        try {
            landCommentService.updateOwn(id, userId, content);
            return landCommentRepository.findById(id)
                    .map(c -> ResponseEntity.ok(ApiResponse.success(toCommentResponse(c, true))))
                    .orElse(ResponseEntity.ok(ApiResponse.error("评论不存在")));
        } catch (IllegalArgumentException landEx) {
            try {
                postService.updateCommentOwn(id, userId, content);
                return postCommentRepository.findById(id)
                        .map(c -> ResponseEntity.ok(ApiResponse.success(toCommentResponse(c, false))))
                        .orElse(ResponseEntity.ok(ApiResponse.error("评论不存在")));
            } catch (IllegalArgumentException postEx) {
                return ResponseEntity.ok(ApiResponse.error(postEx.getMessage()));
            }
        }
    }

    /**
     * App 景点详情（显式返回 latitude/longitude，避免直接序列化 JPA 实体时字段丢失）
     */
    @GetMapping("/landscape-detail/{id}")
    public ResponseEntity<ApiResponse<LandscapeBackendResponse>> publicLandscapeDetail(@PathVariable String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("景点ID不能为空"));
        }
        String landscapeId = id.trim();
        return landscapeService.findApproved(landscapeId)
                .map(l -> ResponseEntity.ok(ApiResponse.success(toLandscapeBackendResponse(l))))
                .orElse(ResponseEntity.ok(ApiResponse.error("景点不存在或未审核")));
    }

    /** 管理员查看景点详情（任意审核状态，HTTP 200 + ApiResponse） */
    @GetMapping("/admin-landscape-detail/{id}")
    public ResponseEntity<ApiResponse<LandscapeResponse>> adminLandscapeDetail(@PathVariable String id) {
        return landscapeService.findById(id)
                .map(l -> ResponseEntity.ok(ApiResponse.success(toAdminLandscapeResponse(l))))
                .orElse(ResponseEntity.ok(ApiResponse.error("景点不存在")));
    }

    /** 管理员查看推荐帖详情（任意审核状态，HTTP 200 + ApiResponse） */
    @GetMapping("/admin-post-detail/{id}")
    public ResponseEntity<ApiResponse<PostBackendResponse>> adminPostDetail(@PathVariable String id) {
        return postService.findById(id)
                .map(p -> ResponseEntity.ok(ApiResponse.success(toAdminPostResponse(p))))
                .orElse(ResponseEntity.ok(ApiResponse.error("推荐帖不存在")));
    }

    /** 已审核通过的景点列表，用于发布推荐帖时关联景点下拉框 */
    @GetMapping("/landscapes/approved")
    public ResponseEntity<ApiResponse<List<Landscape>>> approvedLandscapes() {
        List<Landscape> list = landscapeService.listApproved();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * App 首页：全部已审核景点；keyword 非空时按标题/地址/介绍模糊搜索。
     */
    @GetMapping("/landscapes/home-list")
    public ResponseEntity<ApiResponse<List<LandscapeBackendResponse>>> homeLandscapeList(
            @RequestParam(required = false) String keyword) {
        List<Landscape> list;
        if (keyword == null || keyword.isBlank()) {
            list = landscapeService.listApproved();
        } else {
            list = landscapeService.search(keyword.trim(), "all", "all", "time");
        }
        List<LandscapeBackendResponse> result = list.stream()
                .map(this::toLandscapeBackendResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** 发布推荐帖（提交后进入审核） */
    @PostMapping("/posts/publish")
    public ResponseEntity<ApiResponse<RecommendationPost>> publishPost(
            @RequestBody PostRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("标题不能为空"));
        }
        if (request.getLandscapeId() == null || request.getLandscapeId().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请选择关联景点"));
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("推荐内容不能为空"));
        }

        try {
            postService.createPost(
                    userId,
                    request.getTitle().trim(),
                    request.getLandscapeId().trim(),
                    request.getTag() != null ? request.getTag().trim() : "",
                    request.getContent().trim());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private boolean isProfileAllBlank(UpdateProfileRequest request) {
        return isBlank(request.getRealName())
                && isBlank(request.getPhoneNumber())
                && isBlank(request.getIdNumber())
                && isBlank(request.getGender())
                && isBlank(request.getBirthday());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private CommentResponse toCommentResponse(Object entity, boolean landscape) {
        if (landscape && entity instanceof LandComment c) {
            CommentResponse r = new CommentResponse();
            r.setCommentId(c.getCommentId());
            r.setUserId(c.getUserId());
            sysUserRepository.findById(c.getUserId()).map(SysUser::getUserName).ifPresent(r::setUserName);
            r.setContent(c.getContent());
            r.setPublishTime(c.getPublishTime());
            r.setLandscapeId(c.getLandscapeId());
            return r;
        }
        if (!landscape && entity instanceof PostComment c) {
            CommentResponse r = new CommentResponse();
            r.setCommentId(c.getCommentId());
            r.setUserId(c.getUserId());
            sysUserRepository.findById(c.getUserId()).map(SysUser::getUserName).ifPresent(r::setUserName);
            r.setContent(c.getContent());
            r.setPublishTime(c.getPublishTime());
            r.setPostId(c.getRecomId());
            return r;
        }
        return new CommentResponse();
    }

    private LandscapeBackendResponse toLandscapeBackendResponse(Landscape l) {
        LandscapeBackendResponse r = new LandscapeBackendResponse();
        r.setLandscapeId(l.getLandscapeId());
        r.setUserId(l.getUserId());
        r.setTitle(l.getTitle());
        r.setContent(l.getContent());
        r.setAddress(l.getAddress());
        r.setLatitude(l.getLatitude());
        r.setLongitude(l.getLongitude());
        r.setTel(l.getLandscapeTel());
        r.setOpeningTime(l.getOpeningTime());
        r.setLevel(l.getLevel());
        r.setImagePath(l.getImagePath());
        r.setAuditState(l.getAuditState());
        r.setPublishTime(l.getPublishTime());
        r.setAuditTime(l.getAuditTime());
        r.setLikeCount(landLikeRepository.countByLandscapeId(l.getLandscapeId()));
        r.setCommentCount(landCommentRepository.findByLandscapeIdOrderByPublishTimeDesc(l.getLandscapeId()).size());
        return r;
    }

    private LandscapeResponse toAdminLandscapeResponse(Landscape l) {
        LandscapeResponse r = new LandscapeResponse();
        r.setLandscapeId(l.getLandscapeId());
        r.setTitle(l.getTitle());
        r.setContent(l.getContent());
        r.setAddress(l.getAddress());
        r.setLatitude(l.getLatitude());
        r.setLongitude(l.getLongitude());
        r.setTel(l.getLandscapeTel());
        r.setOpeningTime(l.getOpeningTime());
        r.setLevel(l.getLevel());
        r.setImagePath(l.getImagePath());
        r.setAuditState(l.getAuditState());
        r.setPublishTime(l.getPublishTime());
        r.setLikeCount(landLikeRepository.countByLandscapeId(l.getLandscapeId()));
        return r;
    }

    private PostBackendResponse toAdminPostResponse(RecommendationPost p) {
        PostBackendResponse r = new PostBackendResponse();
        r.setRecomId(p.getRecomId());
        r.setUserId(p.getUserId());
        sysUserRepository.findById(p.getUserId()).ifPresent(u -> r.setUserName(u.getUserName()));
        r.setTitle(p.getTitle());
        r.setLandscapeId(p.getLandscapeId());
        if (p.getLandscapeId() != null) {
            landscapeRepository.findById(p.getLandscapeId()).ifPresent(l -> r.setLandscapeTitle(l.getTitle()));
        }
        r.setTag(p.getTag());
        r.setContent(p.getContent());
        r.setAuditState(p.getAuditState());
        r.setPublishTime(p.getPublishTime());
        return r;
    }

    private UserResponse toProfileResponse(UserProfileService.Profile p) {
        UserResponse r = new UserResponse();
        r.setUserId(p.userId());
        r.setUserName(p.userName());
        r.setRealName(p.realName());
        r.setPhoneNumber(p.phoneNumber());
        r.setIdNumber(p.idNumber());
        r.setGender(p.gender());
        r.setBirthday(p.birthday());
        sysUserRepository.findById(p.userId()).ifPresent(u -> r.setUserType(u.getUserType()));
        return r;
    }
}
