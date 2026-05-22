package com.travel.travelweb.api.dto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostService;

import java.util.List;

/**
 * 推荐帖发布相关 API（供安卓端使用）
 */
@RestController
@RequestMapping("/api")
public class PostPublishController {

    private final PostService postService;
    private final LandscapeService landscapeService;

    public PostPublishController(PostService postService, LandscapeService landscapeService) {
        this.postService = postService;
        this.landscapeService = landscapeService;
    }

    /** 已审核通过的景点列表，用于发布推荐帖时关联景点下拉框 */
    @GetMapping("/landscapes/approved")
    public ResponseEntity<ApiResponse<List<Landscape>>> approvedLandscapes() {
        List<Landscape> list = landscapeService.listApproved();
        return ResponseEntity.ok(ApiResponse.success(list));
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
}
