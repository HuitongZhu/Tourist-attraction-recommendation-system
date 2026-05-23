package com.travel.travelweb.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.travel.travelweb.api.dto.CommentRequest;
import com.travel.travelweb.api.dto.CommentResponse;
import com.travel.travelweb.api.dto.LandscapeRequest;
import com.travel.travelweb.api.dto.PageResponse;
import com.travel.travelweb.api.dto.PostBackendResponse;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.service.LandCommentService;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostService;
import com.travel.travelweb.service.RecommendationPostService;
import com.travel.travelweb.web.dto.LandCommentView;
import com.travel.travelweb.web.dto.PostCommentView;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final LandscapeService landscapeService;
    private final PostService postService;
    private final LandCommentService landCommentService;
    private final RecommendationPostService recommendationPostService;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostCommentRepository postCommentRepository;
    private final LandscapeRepository landscapeRepository;
    private final SysUserRepository sysUserRepository;

    public ApiController(LandscapeService landscapeService, PostService postService, LandCommentService landCommentService,
                         RecommendationPostService recommendationPostService,
                         PostLikeRepository postLikeRepository, PostCollectRepository postCollectRepository,
                         PostCommentRepository postCommentRepository, LandscapeRepository landscapeRepository,
                         SysUserRepository sysUserRepository) {
        this.landscapeService = landscapeService;
        this.postService = postService;
        this.landCommentService = landCommentService;
        this.recommendationPostService = recommendationPostService;
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
        this.postCommentRepository = postCommentRepository;
        this.landscapeRepository = landscapeRepository;
        this.sysUserRepository = sysUserRepository;
    }

    // 首页景点数据
    @GetMapping("/landscapes/home")
    public ResponseEntity<ApiResponse<List<Landscape>>> homeLandscapes(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "8") int size) {
        List<Landscape> landscapes;
        // 如果指定了 status=审核通过，返回所有已审核景点（用于发布推荐帖关联）
        if ("审核通过".equals(status)) {
            landscapes = landscapeService.listApproved();
        } else {
            landscapes = landscapeService.homeLandscapes(size);
        }
        // 按size参数限制数量
        if (size > 0 && landscapes.size() > size) {
            landscapes = landscapes.subList(0, size);
        }
        return ResponseEntity.ok(ApiResponse.success(landscapes));
    }

    // 景点列表/搜索
    @GetMapping("/landscapes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> landscapes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "all") String city,
            @RequestParam(required = false, defaultValue = "all") String level,
            @RequestParam(required = false, defaultValue = "time") String sort,
            @RequestParam(required = false, defaultValue = "10") int size) {
        List<Landscape> landscapes;
        // 如果指定了 status=审核通过，返回所有已审核景点（用于发布推荐帖关联）
        if ("审核通过".equals(status) && (keyword == null || keyword.isBlank())) {
            landscapes = landscapeService.listApproved();
        } else {
            landscapes = landscapeService.search(keyword, city, level, sort);
        }
        // 按size参数限制数量
        if (size > 0 && landscapes.size() > size) {
            landscapes = landscapes.subList(0, size);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("landscapes", landscapes);
        result.put("total", landscapes.size());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 发布景点（JSON）
    @PostMapping(value = "/landscapes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Landscape>> createLandscape(
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

    // 发布景点（含图片，multipart）
    @PostMapping(value = "/landscapes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Landscape>> createLandscapeMultipart(
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
                    userId, title.trim(),
                    content != null ? content.trim() : "",
                    address.trim(), latitude, longitude, tel, openingTime, level, image);
            return landscapeService.findById(id)
                    .map(l -> ResponseEntity.ok(ApiResponse.success(l)))
                    .orElse(ResponseEntity.ok(ApiResponse.error("创建成功但查询失败")));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "发布失败"));
        }
    }

    // 景点详情
    @GetMapping("/landscapes/{id}")
    public ResponseEntity<ApiResponse<Landscape>> landscapeDetail(@PathVariable String id) {
        // 如果是数字ID，按顺序索引查询（App端从0开始）
        if (id.matches("\\d+")) {
            int index = Integer.parseInt(id); // App端从0开始，直接作为列表索引
            List<Landscape> approvedLandscapes = landscapeService.listApproved();
            if (index >= 0 && index < approvedLandscapes.size()) {
                return ResponseEntity.ok(ApiResponse.success(approvedLandscapes.get(index)));
            }
            return ResponseEntity.status(404).body(ApiResponse.error("景点不存在"));
        }
        // 否则按LandscapeID查询（如 LS1779288666316836）
        return landscapeService.findApproved(id)
                .map(landscape -> ResponseEntity.ok(ApiResponse.success(landscape)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("景点不存在或未审核")));
    }

    // 更新景点
    @PutMapping("/landscapes/{id}")
    public ResponseEntity<ApiResponse<Landscape>> updateLandscape(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String tel,
            @RequestParam(required = false) String openingTime,
            @RequestParam(required = false) String level) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        try {
            landscapeService.updateLandscape(id, userId, title, content, address, latitude, longitude, tel, openingTime, level, null);
            return landscapeService.findById(id)
                    .map(landscape -> ResponseEntity.ok(ApiResponse.success(landscape)))
                    .orElse(ResponseEntity.status(404).body(ApiResponse.error("景点不存在")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新失败"));
        }
    }

    // 删除景点
    @DeleteMapping("/landscapes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLandscape(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        try {
            landscapeService.deleteLandscape(id, userId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, e.getMessage()));
        }
    }

    // 推荐帖列表
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> posts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        List<RecommendationPost> posts;
        if (keyword != null && !keyword.isBlank()) {
            // 搜索推荐帖，只返回审核通过的
            posts = recommendationPostService.findPosts(RecommendationPostService.AUDIT_APPROVED, keyword);
        } else {
            // 获取所有审核通过的推荐帖
            posts = postService.listApproved();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("total", posts.size());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 推荐帖详情
    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<PostBackendResponse>> postDetail(@PathVariable String id) {
        return postService.findApproved(id)
                .map(this::toPostResponse)
                .map(post -> ResponseEntity.ok(ApiResponse.success(post)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("推荐帖不存在或未审核")));
    }

    private PostBackendResponse toPostResponse(RecommendationPost p) {
        PostBackendResponse r = new PostBackendResponse();
        r.setRecomId(p.getRecomId());
        r.setUserId(p.getUserId());
        sysUserRepository.findById(p.getUserId()).map(u -> u.getUserName()).ifPresent(r::setUserName);
        r.setTitle(p.getTitle());
        r.setLandscapeId(p.getLandscapeId());
        if (p.getLandscapeId() != null && !p.getLandscapeId().isBlank()) {
            landscapeRepository.findById(p.getLandscapeId())
                    .ifPresent(l -> r.setLandscapeTitle(l.getTitle()));
        }
        r.setTag(p.getTag());
        r.setContent(p.getContent());
        r.setAuditState(p.getAuditState());
        r.setPublishTime(p.getPublishTime());
        r.setLikeCount(postLikeRepository.countByRecomId(p.getRecomId()));
        r.setCommentCount(postCommentRepository.findByRecomIdOrderByPublishTimeDesc(p.getRecomId()).size());
        r.setFavoriteCount(postCollectRepository.countByRecomId(p.getRecomId()));
        return r;
    }

    // 首页推荐帖数据
    @GetMapping("/posts/home")
    public ResponseEntity<ApiResponse<List<PostBackendResponse>>> homePosts() {
        List<PostBackendResponse> posts = postService.homePosts(6).stream()
                .map(this::toPostResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    // 创建评论
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @RequestBody CommentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("评论内容不能为空"));
        }

        try {
            // 判断是景点评论还是帖子评论
            if (request.getLandscapeId() != null && !request.getLandscapeId().isBlank()) {
                landCommentService.add(request.getLandscapeId(), userId, request.getContent());
                List<LandCommentView> comments = landCommentService.listForLandscape(request.getLandscapeId());
                if (!comments.isEmpty()) {
                    LandCommentView latest = comments.get(0);
                    CommentResponse response = new CommentResponse(
                            latest.commentId(),
                            latest.userId(),
                            latest.userName(),
                            latest.content(),
                            latest.publishTime(),
                            request.getLandscapeId(),
                            null
                    );
                    return ResponseEntity.ok(ApiResponse.success(response));
                }
            } else if (request.getPostId() != null && !request.getPostId().isBlank()) {
                postService.addComment(request.getPostId(), userId, request.getContent());
                List<PostCommentView> comments = postService.comments(request.getPostId());
                if (!comments.isEmpty()) {
                    PostCommentView latest = comments.get(0);
                    CommentResponse response = new CommentResponse(
                            latest.commentId(),
                            latest.userId(),
                            latest.userName(),
                            latest.content(),
                            latest.publishTime(),
                            null,
                            request.getPostId()
                    );
                    return ResponseEntity.ok(ApiResponse.success(response));
                }
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("必须指定 landscapeId 或 postId"));
            }
            
            return ResponseEntity.status(500).body(ApiResponse.error("创建评论失败"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取评论列表
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @RequestParam(required = false) String landscapeId,
            @RequestParam(required = false) String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<CommentResponse> comments = new ArrayList<>();
        
        if (landscapeId != null && !landscapeId.isBlank()) {
            List<LandCommentView> landComments = landCommentService.listForLandscape(landscapeId);
            for (LandCommentView view : landComments) {
                comments.add(new CommentResponse(
                        view.commentId(),
                        view.userId(),
                        view.userName(),
                        view.content(),
                        view.publishTime(),
                        landscapeId,
                        null
                ));
            }
        } else if (postId != null && !postId.isBlank()) {
            List<PostCommentView> postComments = postService.comments(postId);
            for (PostCommentView view : postComments) {
                comments.add(new CommentResponse(
                        view.commentId(),
                        view.userId(),
                        view.userName(),
                        view.content(),
                        view.publishTime(),
                        null,
                        postId
                ));
            }
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("必须指定 landscapeId 或 postId"));
        }

        // 分页处理
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, comments.size());
        
        List<CommentResponse> pageContent = fromIndex < comments.size() 
                ? comments.subList(fromIndex, toIndex) 
                : new ArrayList<>();

        PageResponse<CommentResponse> pageResponse = new PageResponse<>(
                pageContent,
                page,
                size,
                comments.size()
        );

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    // 删除评论
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }

        try {
            // 尝试删除景点评论
            try {
                landCommentService.deleteOwn(id, userId);
                return ResponseEntity.ok(ApiResponse.success(null));
            } catch (IllegalArgumentException e) {
                // 景点评论删除失败，尝试删除帖子评论
                try {
                    postService.deleteCommentOwn(id, userId);
                    return ResponseEntity.ok(ApiResponse.success(null));
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.status(403).body(ApiResponse.error(403, ex.getMessage()));
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("删除评论失败"));
        }
    }
}
