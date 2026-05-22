package com.travel.travelweb.api.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.travel.travelweb.service.LandCommentService;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostService;
import com.travel.travelweb.service.RecommendationPostService;

/**
 * 管理员审核：景点、推荐帖、评论（供安卓端）
 */
@RestController
@RequestMapping("/api/admin/review")
public class AdminReviewController {

    private final LandscapeService landscapeService;
    private final LandscapeRepository landscapeRepository;
    private final RecommendationPostService recommendationPostService;
    private final RecommendationPostRepository postRepository;
    private final LandCommentRepository landCommentRepository;
    private final PostCommentRepository postCommentRepository;
    private final LandCommentService landCommentService;
    private final PostService postService;
    private final SysUserRepository sysUserRepository;
    private final LandLikeRepository landLikeRepository;

    public AdminReviewController(
            LandscapeService landscapeService,
            LandscapeRepository landscapeRepository,
            RecommendationPostService recommendationPostService,
            RecommendationPostRepository postRepository,
            LandCommentRepository landCommentRepository,
            PostCommentRepository postCommentRepository,
            LandCommentService landCommentService,
            PostService postService,
            SysUserRepository sysUserRepository,
            LandLikeRepository landLikeRepository) {
        this.landscapeService = landscapeService;
        this.landscapeRepository = landscapeRepository;
        this.recommendationPostService = recommendationPostService;
        this.postRepository = postRepository;
        this.landCommentRepository = landCommentRepository;
        this.postCommentRepository = postCommentRepository;
        this.landCommentService = landCommentService;
        this.postService = postService;
        this.sysUserRepository = sysUserRepository;
        this.landLikeRepository = landLikeRepository;
    }

    /** filter: all | approved | pending */
    @GetMapping("/landscapes")
    public ResponseEntity<ApiResponse<List<LandscapeResponse>>> reviewLandscapes(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String keyword) {
        List<Landscape> all = landscapeService.findAllForAdmin(null, keyword);
        String f = ReviewFilterHelper.mapFilter(filter);
        List<Landscape> list = all.stream()
                .filter(l -> ReviewFilterHelper.matchesFilter(l.getAuditState(), f))
                .sorted(Comparator.comparingInt(l -> ReviewFilterHelper.sortOrder(l.getAuditState())))
                .collect(Collectors.toList());
        List<LandscapeResponse> result = list.stream().map(this::toLandscapeResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/landscapes/{id}/audit")
    public ResponseEntity<ApiResponse<LandscapeResponse>> auditLandscape(
            @PathVariable String id,
            @RequestBody AuditRequest request) {
        String status = ReviewFilterHelper.resolveAuditStatus(request);
        if (status == null) {
            return ResponseEntity.ok(ApiResponse.error("无效的审核参数"));
        }
        if (!landscapeService.updateAuditState(id, status)) {
            return ResponseEntity.ok(ApiResponse.error("审核失败"));
        }
        return landscapeRepository.findById(id)
                .map(l -> ResponseEntity.ok(ApiResponse.success(toLandscapeResponse(l))))
                .orElse(ResponseEntity.ok(ApiResponse.error("景点不存在")));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostBackendResponse>>> reviewPosts(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String keyword) {
        List<RecommendationPost> all = recommendationPostService.findPosts(null, keyword);
        String f = ReviewFilterHelper.mapFilter(filter);
        List<RecommendationPost> list = all.stream()
                .filter(p -> ReviewFilterHelper.matchesFilter(p.getAuditState(), f))
                .sorted(Comparator.comparingInt(p -> ReviewFilterHelper.sortOrder(p.getAuditState())))
                .collect(Collectors.toList());
        List<PostBackendResponse> result = list.stream().map(this::toPostResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/posts/{id}/audit")
    public ResponseEntity<ApiResponse<PostBackendResponse>> auditPost(
            @PathVariable String id,
            @RequestBody AuditRequest request) {
        String status = ReviewFilterHelper.resolveAuditStatus(request);
        if (status == null) {
            return ResponseEntity.ok(ApiResponse.error("无效的审核参数"));
        }
        if (!recommendationPostService.updateAuditState(id, status)) {
            return ResponseEntity.ok(ApiResponse.error("审核失败"));
        }
        return postRepository.findById(id)
                .map(p -> ResponseEntity.ok(ApiResponse.success(toPostResponse(p))))
                .orElse(ResponseEntity.ok(ApiResponse.error("推荐帖不存在")));
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<List<CommentReviewResponse>>> reviewComments(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String keyword) {
        List<CommentReviewResponse> all = loadAllComments(keyword);
        String f = ReviewFilterHelper.mapFilter(filter);
        List<CommentReviewResponse> list = all.stream()
                .filter(c -> ReviewFilterHelper.matchesFilter(c.getAuditState(), f))
                .sorted(Comparator.comparingInt(c -> ReviewFilterHelper.sortOrder(c.getAuditState())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PatchMapping("/comments/{id}/audit")
    public ResponseEntity<ApiResponse<Void>> auditComment(
            @PathVariable String id,
            @RequestBody AuditRequest request) {
        String status = ReviewFilterHelper.resolveAuditStatus(request);
        if (status == null) {
            return ResponseEntity.ok(ApiResponse.error("无效的审核参数"));
        }
        if (ReviewFilterHelper.isApproved(status)) {
            CommentAuditStore.markApproved(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        if (ReviewFilterHelper.isRejected(status)) {
            CommentAuditStore.unmark(id);
            if (landCommentService.deleteByAdmin(id)) {
                return ResponseEntity.ok(ApiResponse.success(null));
            }
            if (postService.deleteCommentByAdmin(id)) {
                return ResponseEntity.ok(ApiResponse.success(null));
            }
            return ResponseEntity.ok(ApiResponse.error("评论不存在"));
        }
        CommentAuditStore.unmark(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private List<CommentReviewResponse> loadAllComments(String keyword) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        List<CommentReviewResponse> list = new ArrayList<>();
        for (LandComment c : landCommentRepository.findAll()) {
            CommentReviewResponse r = toLandCommentReview(c);
            if (matchesKeyword(r, kw)) {
                list.add(r);
            }
        }
        for (PostComment c : postCommentRepository.findAll()) {
            CommentReviewResponse r = toPostCommentReview(c);
            if (matchesKeyword(r, kw)) {
                list.add(r);
            }
        }
        return list;
    }

    private boolean matchesKeyword(CommentReviewResponse r, String kw) {
        if (kw.isEmpty()) {
            return true;
        }
        return contains(r.getContent(), kw)
                || contains(r.getUserName(), kw)
                || contains(r.getTargetTitle(), kw);
    }

    private boolean contains(String value, String kw) {
        return value != null && value.toLowerCase().contains(kw);
    }

    private CommentReviewResponse toLandCommentReview(LandComment c) {
        CommentReviewResponse r = new CommentReviewResponse();
        r.setCommentId(c.getCommentId());
        r.setUserId(c.getUserId());
        r.setUserName(resolveUserName(c.getUserId()));
        r.setContent(c.getContent());
        r.setPublishTime(c.getPublishTime());
        r.setLandscapeId(c.getLandscapeId());
        r.setTargetType("景点");
        landscapeRepository.findById(c.getLandscapeId()).ifPresent(l -> {
            r.setTargetTitle(l.getTitle());
        });
        r.setAuditState(CommentAuditStore.commentAuditState(c.getCommentId()));
        return r;
    }

    private CommentReviewResponse toPostCommentReview(PostComment c) {
        CommentReviewResponse r = new CommentReviewResponse();
        r.setCommentId(c.getCommentId());
        r.setUserId(c.getUserId());
        r.setUserName(resolveUserName(c.getUserId()));
        r.setContent(c.getContent());
        r.setPublishTime(c.getPublishTime());
        r.setPostId(c.getRecomId());
        r.setTargetType("推荐帖");
        postRepository.findById(c.getRecomId()).ifPresent(p -> {
            String title = p.getTitle();
            if (title == null || title.isBlank()) {
                title = p.getTag();
            }
            r.setTargetTitle(title);
        });
        r.setAuditState(CommentAuditStore.commentAuditState(c.getCommentId()));
        return r;
    }

    private String resolveUserName(String userId) {
        if (userId == null) {
            return "未知";
        }
        return sysUserRepository.findById(userId)
                .map(SysUser::getUserName)
                .orElse(userId);
    }

    private LandscapeResponse toLandscapeResponse(Landscape l) {
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

    private PostBackendResponse toPostResponse(RecommendationPost p) {
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
}
