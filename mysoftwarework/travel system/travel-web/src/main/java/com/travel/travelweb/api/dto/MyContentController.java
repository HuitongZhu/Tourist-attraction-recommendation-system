package com.travel.travelweb.api.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostService;

/**
 * 个人中心：我的景点、我的推荐帖（供安卓端）
 */
@RestController
@RequestMapping("/api/my")
public class MyContentController {

    private final LandscapeRepository landscapeRepository;
    private final LandscapeService landscapeService;
    private final PostService postService;
    private final SysUserRepository sysUserRepository;
    private final LandLikeRepository landLikeRepository;
    private final LandCommentRepository landCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;

    public MyContentController(
            LandscapeRepository landscapeRepository,
            LandscapeService landscapeService,
            PostService postService,
            SysUserRepository sysUserRepository,
            LandLikeRepository landLikeRepository,
            LandCommentRepository landCommentRepository,
            PostLikeRepository postLikeRepository,
            PostCommentRepository postCommentRepository) {
        this.landscapeRepository = landscapeRepository;
        this.landscapeService = landscapeService;
        this.postService = postService;
        this.sysUserRepository = sysUserRepository;
        this.landLikeRepository = landLikeRepository;
        this.landCommentRepository = landCommentRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCommentRepository = postCommentRepository;
    }

    /** 当前用户发布的全部景点（含各审核状态） */
    @GetMapping("/landscapes")
    public ResponseEntity<ApiResponse<List<LandscapeBackendResponse>>> myLandscapes(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        List<LandscapeBackendResponse> list = landscapeRepository.findByUserId(userId).stream()
                .map(this::toLandscapeResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /** 当前用户某一景点详情（用于编辑） */
    @GetMapping("/landscapes/{id}")
    public ResponseEntity<ApiResponse<LandscapeBackendResponse>> myLandscapeDetail(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        Optional<Landscape> l = landscapeRepository.findById(id);
        if (l.isEmpty() || !userId.equals(l.get().getUserId())) {
            return ResponseEntity.status(404).body(ApiResponse.error("景点不存在或无权查看"));
        }
        return ResponseEntity.ok(ApiResponse.success(toLandscapeResponse(l.get())));
    }

    /**
     * 修改景点：保存后状态变为「待审核」，需管理员再审。
     */
    @PutMapping("/landscapes/{id}")
    public ResponseEntity<ApiResponse<LandscapeBackendResponse>> updateMyLandscape(
            @PathVariable String id,
            @RequestBody LandscapeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        try {
            landscapeService.updateLandscape(
                    id,
                    userId,
                    request.getTitle(),
                    request.getContent(),
                    request.getAddress(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.resolveTel(),
                    request.getOpeningTime(),
                    request.getLevel(),
                    null);
            return landscapeRepository.findById(id)
                    .filter(l -> userId.equals(l.getUserId()))
                    .map(l -> ResponseEntity.ok(ApiResponse.success(toLandscapeResponse(l))))
                    .orElse(ResponseEntity.status(404).body(ApiResponse.error("景点不存在")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新失败"));
        }
    }

    @DeleteMapping("/landscapes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMyLandscape(
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

    /** 当前用户发布的全部推荐帖 */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostBackendResponse>>> myPosts(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        List<PostBackendResponse> list = new ArrayList<>();
        for (RecommendationPost p : postService.findByUserId(userId)) {
            list.add(toPostResponse(p));
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    private LandscapeBackendResponse toLandscapeResponse(Landscape l) {
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

    private PostBackendResponse toPostResponse(RecommendationPost p) {
        PostBackendResponse r = new PostBackendResponse();
        r.setRecomId(p.getRecomId());
        r.setUserId(p.getUserId());
        sysUserRepository.findById(p.getUserId()).map(SysUser::getUserName).ifPresent(r::setUserName);
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
        return r;
    }
}
