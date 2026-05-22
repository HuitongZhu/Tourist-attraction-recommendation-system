package com.travel.travelweb.api;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travel.travelweb.api.dto.*;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.service.*;

@RestController
@RequestMapping("/api")
public class AppApiController {

    private final AuthService authService;
    private final LandscapeService landscapeService;
    private final PostService postService;
    private final LandCommentService landCommentService;
    private final LandInteractionService landInteractionService;
    private final PostInteractionService postInteractionService;
    private final UserService userService;
    private final AmapService amapService;
    private final SysUserRepository sysUserRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final LandLikeRepository landLikeRepository;
    private final LandCommentRepository landCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final LandCollectRepository landCollectRepository;
    private final PostCollectRepository postCollectRepository;
    private final RecommendationPostRepository postRepository;

    public AppApiController(AuthService authService, LandscapeService landscapeService, PostService postService,
                           LandCommentService landCommentService, LandInteractionService landInteractionService,
                           PostInteractionService postInteractionService, UserService userService,
                           AmapService amapService, SysUserRepository sysUserRepository,
                           OrdinaryUserRepository ordinaryUserRepository, LandLikeRepository landLikeRepository,
                           LandCommentRepository landCommentRepository, PostLikeRepository postLikeRepository,
                           LandCollectRepository landCollectRepository, PostCollectRepository postCollectRepository,
                           RecommendationPostRepository postRepository) {
        this.authService = authService;
        this.landscapeService = landscapeService;
        this.postService = postService;
        this.landCommentService = landCommentService;
        this.landInteractionService = landInteractionService;
        this.postInteractionService = postInteractionService;
        this.userService = userService;
        this.amapService = amapService;
        this.sysUserRepository = sysUserRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.landLikeRepository = landLikeRepository;
        this.landCommentRepository = landCommentRepository;
        this.postLikeRepository = postLikeRepository;
        this.landCollectRepository = landCollectRepository;
        this.postCollectRepository = postCollectRepository;
        this.postRepository = postRepository;
    }

    // --- 认证模块 ---
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestParam String account,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "2") String userType,
            @RequestParam(defaultValue = "password") String loginType) {
        
        Optional<SysUser> u;
        
        if ("sms".equals(loginType)) {
            u = authService.loginBySmsCode(account, code, userType);
        } else {
            u = authService.login(account, password, userType);
        }
        
        if (u.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("账号或密码错误，或所选身份与账号类型不匹配"));
        }
        
        SysUser user = u.get();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("userType", user.getUserType());
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestParam String userName,
            @RequestParam String account,
            @RequestParam String password,
            @RequestParam String confirm_password) {
        
        if (!password.equals(confirm_password)) {
            return ResponseEntity.ok(ApiResponse.error("两次输入的密码不一致"));
        }
        
        try {
            String userId = authService.register(userName.trim(), account.trim(), password);
            return ResponseEntity.ok(ApiResponse.success(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/send-sms-code")
    public ResponseEntity<ApiResponse<String>> sendSms(@RequestParam String phone) {
        try {
            String code = authService.sendSmsCode(phone);
            return ResponseEntity.ok(ApiResponse.success("验证码已发送"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register/send-code")
    public ResponseEntity<ApiResponse<String>> sendRegisterCode(@RequestParam String phone) {
        try {
            String code = authService.sendRegisterSmsCode(phone);
            return ResponseEntity.ok(ApiResponse.success("验证码已发送"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register/verify-code")
    public ResponseEntity<ApiResponse<Boolean>> verifyRegisterCode(
            @RequestParam String phone,
            @RequestParam String code) {
        try {
            boolean valid = authService.verifyRegisterCode(phone, code);
            return ResponseEntity.ok(ApiResponse.success(valid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    // --- 景点模块 ---
    // 注意：/api/landscapes/home、/api/landscapes、/api/landscapes/{id} 列表/详情在 ApiController

    /**
     * 安卓发布景点（POST JSON）。与 ApiController#createLandscape 等价。
     * 路径 /landscapes/publish 便于确认新后端已部署（旧服务仅 Allow:GET）。
     */
    @PostMapping(value = "/landscapes/publish", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Landscape>> publishLandscape(
            @RequestBody LandscapeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("景点名称不能为空"));
        }
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("景点地点不能为空"));
        }
        try {
            String id = landscapeService.createLandscape(
                    userId,
                    request.getTitle().trim(),
                    request.getContent() != null ? request.getContent().trim() : "",
                    request.getAddress().trim(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.resolveTel(),
                    request.getOpeningTime(),
                    request.getLevel(),
                    null);
            return landscapeService.findById(id)
                    .map(l -> ResponseEntity.ok(ApiResponse.success(l)))
                    .orElse(ResponseEntity.ok(ApiResponse.error("创建成功但查询失败")));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "发布失败"));
        }
    }

    /** 安卓发布景点（含图片上传，multipart/form-data） */
    @PostMapping(value = "/landscapes/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Landscape>> publishLandscapeMultipart(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String title,
            @RequestParam String address,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String tel,
            @RequestParam(required = false) String openingTime,
            @RequestParam(required = false) String level,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("景点名称不能为空"));
        }
        if (address == null || address.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("景点地点不能为空"));
        }
        try {
            String id = landscapeService.createLandscape(
                    userId,
                    title.trim(),
                    content != null ? content.trim() : "",
                    address.trim(),
                    latitude,
                    longitude,
                    tel,
                    openingTime,
                    level,
                    image);
            return landscapeService.findById(id)
                    .map(l -> ResponseEntity.ok(ApiResponse.success(l)))
                    .orElse(ResponseEntity.ok(ApiResponse.error("创建成功但查询失败")));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "发布失败"));
        }
    }

    private LandscapeResponse toLandscapeResponse(Landscape l) {
        LandscapeResponse response = new LandscapeResponse();
        response.setLandscapeId(l.getLandscapeId());
        response.setTitle(l.getTitle());
        response.setContent(l.getContent());
        response.setAddress(l.getAddress());
        response.setLatitude(l.getLatitude());
        response.setLongitude(l.getLongitude());
        response.setTel(l.getLandscapeTel());
        response.setOpeningTime(l.getOpeningTime());
        response.setLevel(l.getLevel());
        response.setImagePath(l.getImagePath());
        response.setAuditState(l.getAuditState());
        response.setPublishTime(l.getPublishTime());
        response.setLikeCount(landLikeRepository.countByLandscapeId(l.getLandscapeId()));
        response.setCommentCount(landCommentRepository.findByLandscapeIdOrderByPublishTimeDesc(l.getLandscapeId()).size());
        return response;
    }

    private LandscapeBackendResponse toLandscapeBackendResponse(Landscape l) {
        LandscapeBackendResponse response = new LandscapeBackendResponse();
        response.setLandscapeId(l.getLandscapeId());
        response.setUserId(l.getUserId());
        response.setTitle(l.getTitle());
        response.setContent(l.getContent());
        response.setAddress(l.getAddress());
        response.setLatitude(l.getLatitude());
        response.setLongitude(l.getLongitude());
        response.setTel(l.getLandscapeTel());
        response.setOpeningTime(l.getOpeningTime());
        response.setLevel(l.getLevel());
        response.setImagePath(l.getImagePath());
        response.setAuditState(l.getAuditState());
        response.setPublishTime(l.getPublishTime());
        response.setAuditTime(l.getAuditTime());
        response.setLikeCount(landLikeRepository.countByLandscapeId(l.getLandscapeId()));
        response.setCommentCount(landCommentRepository.findByLandscapeIdOrderByPublishTimeDesc(l.getLandscapeId()).size());
        return response;
    }

    // --- 帖子模块 ---
    // 注意：/api/posts 和 /api/posts/{id} 接口已在 ApiController 中定义，此处不再重复

    // --- 评论模块 ---
    // 注意：/api/comments 接口已在 ApiController 中定义，此处不再重复

    // --- 点赞收藏模块 ---
    @GetMapping("/interactions/status")
    public ResponseEntity<ApiResponse<InteractionStatusResponse>> interactionStatus(
            @RequestParam(required = false) String landscapeId,
            @RequestParam(required = false) String postId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }

        InteractionStatusResponse status = new InteractionStatusResponse();
        if (landscapeId != null && !landscapeId.isBlank()) {
            status.setLiked(landInteractionService.liked(landscapeId, userId));
            status.setFavorited(landInteractionService.collected(landscapeId, userId));
            landLikeRepository.findByLandscapeIdAndUserId(landscapeId, userId)
                    .ifPresent(l -> status.setLikeId(l.getLikeId()));
            landCollectRepository.findByLandscapeIdAndUserId(landscapeId, userId)
                    .ifPresent(c -> status.setFavoriteId(c.getCollectId()));
        } else if (postId != null && !postId.isBlank()) {
            status.setLiked(postInteractionService.liked(postId, userId));
            status.setFavorited(postInteractionService.collected(postId, userId));
            postLikeRepository.findByRecomIdAndUserId(postId, userId)
                    .ifPresent(l -> status.setLikeId(l.getLikeId()));
            postCollectRepository.findByRecomIdAndUserId(postId, userId)
                    .ifPresent(c -> status.setFavoriteId(c.getCollectId()));
        } else {
            return ResponseEntity.ok(ApiResponse.error("必须指定 landscapeId 或 postId"));
        }
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/users/me/interactions/landscapes/likes")
    public ResponseEntity<ApiResponse<List<LandscapeBackendResponse>>> myLandscapeLikes(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        List<LandscapeBackendResponse> result = new ArrayList<>();
        for (var like : landLikeRepository.findByUserIdOrderByLikeTimeDesc(userId)) {
            landscapeService.findApproved(like.getLandscapeId())
                    .map(this::toLandscapeBackendResponse)
                    .ifPresent(result::add);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/users/me/interactions/landscapes/favorites")
    public ResponseEntity<ApiResponse<List<LandscapeBackendResponse>>> myLandscapeFavorites(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        List<LandscapeBackendResponse> result = new ArrayList<>();
        for (var collect : landCollectRepository.findByUserIdOrderByCollectTimeDesc(userId)) {
            landscapeService.findApproved(collect.getLandscapeId())
                    .map(this::toLandscapeBackendResponse)
                    .ifPresent(result::add);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/users/me/interactions/posts/likes")
    public ResponseEntity<ApiResponse<List<RecommendationPost>>> myPostLikes(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        List<RecommendationPost> result = new ArrayList<>();
        for (var like : postLikeRepository.findByUserIdOrderByLikeTimeDesc(userId)) {
            postService.findApproved(like.getRecomId()).ifPresent(result::add);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/users/me/interactions/posts/favorites")
    public ResponseEntity<ApiResponse<List<RecommendationPost>>> myPostFavorites(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        List<RecommendationPost> result = new ArrayList<>();
        for (var collect : postCollectRepository.findByUserIdOrderByCollectTimeDesc(userId)) {
            postService.findApproved(collect.getRecomId()).ifPresent(result::add);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/interactions/favorites")
    public ResponseEntity<ApiResponse<InteractionResponse>> addFavorite(
            @RequestBody FavoriteRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        
        String targetId = InteractionRequestSupport.resolveTargetId(request);
        String targetType = request.getTargetType();
        
        if (targetId == null || targetId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("缺少目标 ID"));
        }
        
        try {
            if (InteractionRequestSupport.isLandscapeType(targetType)) {
                boolean collected = landInteractionService.toggleCollect(targetId, userId);
                if (collected) {
                    String collectId = landCollectRepository
                            .findByLandscapeIdAndUserId(targetId, userId)
                            .map(com.travel.travelweb.entity.LandCollect::getCollectId)
                            .orElse(null);
                    return ResponseEntity.ok(ApiResponse.success(
                            new InteractionResponse(collectId, "LANDSCAPE", targetId, null, request.getLinkUrl())));
                }
                return ResponseEntity.ok(ApiResponse.success(null));
            } else if (InteractionRequestSupport.isPostType(targetType)) {
                boolean collected = postInteractionService.toggleCollect(targetId, userId);
                if (collected) {
                    String collectId = postCollectRepository
                            .findByRecomIdAndUserId(targetId, userId)
                            .map(com.travel.travelweb.entity.PostCollect::getCollectId)
                            .orElse(null);
                    return ResponseEntity.ok(ApiResponse.success(
                            new InteractionResponse(collectId, "POST", null, targetId, request.getLinkUrl())));
                }
                return ResponseEntity.ok(ApiResponse.success(null));
            }
            return ResponseEntity.ok(ApiResponse.error("targetType 无效，应为 LANDSCAPE 或 POST"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("操作失败"));
        }
    }

    @DeleteMapping("/interactions/favorites/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        
        // 尝试删除帖子收藏
        postCollectRepository.findById(id).ifPresent(c -> {
            if (userId.equals(c.getUserId())) {
                postCollectRepository.delete(c);
            }
        });
        
        // 尝试删除景点收藏
        landCollectRepository.findById(id).ifPresent(c -> {
            if (userId.equals(c.getUserId())) {
                landCollectRepository.delete(c);
            }
        });
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/interactions/likes")
    public ResponseEntity<ApiResponse<InteractionResponse>> addLike(
            @RequestBody LikeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        
        String targetId = InteractionRequestSupport.resolveTargetId(request);
        String targetType = request.getTargetType();
        
        if (targetId == null || targetId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error("缺少目标 ID"));
        }
        
        try {
            if (InteractionRequestSupport.isLandscapeType(targetType)) {
                boolean liked = landInteractionService.toggleLike(targetId, userId);
                if (liked) {
                    String likeId = landLikeRepository
                            .findByLandscapeIdAndUserId(targetId, userId)
                            .map(com.travel.travelweb.entity.LandLike::getLikeId)
                            .orElse(null);
                    return ResponseEntity.ok(ApiResponse.success(
                            new InteractionResponse(likeId, "LANDSCAPE", targetId, null, request.getLinkUrl())));
                }
                return ResponseEntity.ok(ApiResponse.success(null));
            } else if (InteractionRequestSupport.isPostType(targetType)) {
                boolean liked = postInteractionService.toggleLike(targetId, userId);
                if (liked) {
                    String likeId = postLikeRepository
                            .findByRecomIdAndUserId(targetId, userId)
                            .map(com.travel.travelweb.entity.PostLike::getLikeId)
                            .orElse(null);
                    return ResponseEntity.ok(ApiResponse.success(
                            new InteractionResponse(likeId, "POST", null, targetId, request.getLinkUrl())));
                }
                return ResponseEntity.ok(ApiResponse.success(null));
            }
            return ResponseEntity.ok(ApiResponse.error("targetType 无效，应为 LANDSCAPE 或 POST"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("操作失败"));
        }
    }

    @DeleteMapping("/interactions/likes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLike(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        
        // 尝试删除帖子点赞
        postLikeRepository.findById(id).ifPresent(l -> {
            if (userId.equals(l.getUserId())) {
                postLikeRepository.delete(l);
            }
        });
        
        // 尝试删除景点点赞
        landLikeRepository.findById(id).ifPresent(l -> {
            if (userId.equals(l.getUserId())) {
                landLikeRepository.delete(l);
            }
        });
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- 用户模块 ---
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        
        return sysUserRepository.findById(userId)
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setUserId(user.getUserId());
                    response.setUserName(user.getUserName());
                    response.setUserType(user.getUserType());
                    ordinaryUserRepository.findById(userId)
                            .ifPresent(ou -> response.setPhoneNumber(ou.getPhoneNumber()));
                    return ResponseEntity.ok(ApiResponse.success(response));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("用户不存在")));
    }

    @PutMapping("/users/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(401, "用户未登录"));
        }
        
        try {
            boolean updated = userService.updateUserName(userId, request.getUserName());
            if (updated) {
                return getCurrentUser(userId);
            }
            return ResponseEntity.ok(ApiResponse.error("更新失败"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    // --- 管理员用户管理 ---
    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        
        List<SysUser> users = userService.findOrdinaryUsers(keyword);
        
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, users.size());
        List<SysUser> pageContent = fromIndex < users.size() 
                ? users.subList(fromIndex, toIndex) 
                : List.of();
        
        List<UserResponse> responseList = pageContent.stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setUserId(user.getUserId());
                    response.setUserName(user.getUserName());
                    response.setUserType(user.getUserType());
                    ordinaryUserRepository.findById(user.getUserId())
                            .ifPresent(ou -> response.setPhoneNumber(ou.getPhoneNumber()));
                    return response;
                })
                .collect(Collectors.toList());
        
        PageResponse<UserResponse> pageResponse = new PageResponse<>(
                responseList, page, size, users.size());
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        return sysUserRepository.findById(id)
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setUserId(user.getUserId());
                    response.setUserName(user.getUserName());
                    response.setUserType(user.getUserType());
                    ordinaryUserRepository.findById(user.getUserId())
                            .ifPresent(ou -> response.setPhoneNumber(ou.getPhoneNumber()));
                    return ResponseEntity.ok(ApiResponse.success(response));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("用户不存在")));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.error("删除失败"));
    }

    // --- 地图模块 ---
    @GetMapping("/maps/geocode")
    public ResponseEntity<ApiResponse<GeocodeResponse>> geocode(@RequestParam String address) {
        try {
            Map<String, Double> result = amapService.getCoordinates(address);
            Double lat = result.get("latitude");
            Double lng = result.get("longitude");
            GeocodeResponse response = new GeocodeResponse(lat, lng, address);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/maps/geocode")
    public ResponseEntity<ApiResponse<GeocodeResponse>> geocodePost(@RequestBody GeocodeRequest request) {
        return geocode(request.getAddress());
    }

    // --- 管理员审核模块 ---
    @GetMapping("/admin/landscapes")
    public ResponseEntity<ApiResponse<PageResponse<LandscapeResponse>>> getAllLandscapes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<Landscape> landscapes = landscapeService.findAllForAdmin(null, null);
        
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, landscapes.size());
        List<Landscape> pageContent = fromIndex < landscapes.size() 
                ? landscapes.subList(fromIndex, toIndex) 
                : List.of();
        
        List<LandscapeResponse> responseList = pageContent.stream()
                .map(this::toLandscapeResponse)
                .collect(Collectors.toList());
        
        PageResponse<LandscapeResponse> pageResponse = new PageResponse<>(
                responseList, page, size, landscapes.size());
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    // 注意：/api/admin/posts 接口已在 AdminController 中定义，此处不再重复

    @PatchMapping("/admin/landscapes/{id}/audit")
    public ResponseEntity<ApiResponse<LandscapeResponse>> auditLandscape(
            @PathVariable String id,
            @RequestBody AuditRequest request) {
        
        String status = request.getStatus();
        boolean updated = landscapeService.updateAuditState(id, status);
        
        if (updated) {
            return landscapeService.findById(id)
                    .map(l -> ResponseEntity.ok(ApiResponse.success(toLandscapeResponse(l))))
                    .orElse(ResponseEntity.ok(ApiResponse.error("景点不存在")));
        }
        return ResponseEntity.ok(ApiResponse.error("审核失败"));
    }

    // 注意：/api/admin/posts/{id}/audit 接口已在 AdminController 中定义，此处不再重复
}
