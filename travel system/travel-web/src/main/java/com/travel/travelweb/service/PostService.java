package com.travel.travelweb.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.travelweb.entity.Comment;
import com.travel.travelweb.entity.PostComment;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.repo.CollectRepository;
import com.travel.travelweb.repo.CommentRepository;
import com.travel.travelweb.repo.LikeRepository;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.util.IdGenerator;
import com.travel.travelweb.web.dto.PostCommentView;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    public static final String AUDIT_APPROVED = "审核通过";

    private final RecommendationPostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final SysUserRepository sysUserRepository;
    private final LandscapeRepository landscapeRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final CollectRepository collectRepository;

    public PostService(
            RecommendationPostRepository postRepository,
            PostCommentRepository postCommentRepository,
            PostLikeRepository postLikeRepository,
            PostCollectRepository postCollectRepository,
            SysUserRepository sysUserRepository,
            LandscapeRepository landscapeRepository,
            CommentRepository commentRepository,
            LikeRepository likeRepository,
            CollectRepository collectRepository) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
        this.sysUserRepository = sysUserRepository;
        this.landscapeRepository = landscapeRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.collectRepository = collectRepository;
    }

    public List<RecommendationPost> homePosts(int limit) {
        return postRepository.findByAuditStateOrderByPublishTimeDesc(AUDIT_APPROVED, PageRequest.of(0, limit));
    }

    public List<RecommendationPost> listApproved() {
        return postRepository.findByAuditStateOrderByPublishTimeDesc(AUDIT_APPROVED, PageRequest.of(0, 500));
    }

    public List<RecommendationPost> searchApproved(String keyword) {
        String kw = keyword != null ? keyword.trim() : "";
        return postRepository.searchPosts(AUDIT_APPROVED, kw);
    }

    public Optional<RecommendationPost> findApproved(String id) {
        return postRepository.findById(id).filter(p -> AUDIT_APPROVED.equals(p.getAuditState()));
    }

    public Optional<RecommendationPost> findById(String id) {
        return postRepository.findById(id);
    }

    public List<RecommendationPost> findByUserId(String userId) {
        return postRepository.findByUserId(userId);
    }

    public List<PostCommentView> comments(String recomId) {
        return postCommentRepository.findByRecomIdOrderByPublishTimeDesc(recomId).stream()
                .map(c -> {
                    String name = sysUserRepository.findById(c.getUserId())
                            .map(u -> u.getUserName() != null ? u.getUserName() : u.getUserId())
                            .orElse("用户");
                    return new PostCommentView(c.getCommentId(), c.getUserId(), name, c.getContent(), c.getPublishTime());
                })
                .toList();
    }

    @Transactional
    public void addComment(String recomId, String userId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论不能为空");
        }
        String commentId = IdGenerator.next("PCM");
        PostComment c = new PostComment();
        c.setCommentId(commentId);
        c.setRecomId(recomId);
        c.setUserId(userId);
        c.setContent(content.trim());
        c.setPublishTime(LocalDateTime.now());
        postCommentRepository.save(c);
        
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setPublishTime(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public Optional<String> findRecomIdForComment(String commentId) {
        return postCommentRepository.findById(commentId).map(PostComment::getRecomId);
    }

    @Transactional
    public void updateCommentOwn(String commentId, String userId, String content) {
        PostComment c = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        if (!userId.equals(c.getUserId())) {
            throw new IllegalArgumentException("无权修改该评论");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        c.setContent(content.trim());
        c.setPublishTime(LocalDateTime.now());
        postCommentRepository.save(c);
        
        commentRepository.findById(commentId).ifPresent(comment -> {
            comment.setContent(content.trim());
            comment.setPublishTime(LocalDateTime.now());
            commentRepository.save(comment);
        });
    }

    @Transactional
    public void deleteCommentOwn(String commentId, String userId) {
        PostComment c = postCommentRepository.findById(commentId).orElseThrow();
        if (!userId.equals(c.getUserId())) {
            throw new IllegalArgumentException("无权删除该评论");
        }
        postCommentRepository.delete(c);
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public boolean deleteCommentByAdmin(String commentId) {
        return postCommentRepository.findById(commentId).map(c -> {
            postCommentRepository.delete(c);
            commentRepository.deleteById(commentId);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void createPost(String userId, String title, String landscapeId, String tag, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        if (landscapeId == null || landscapeId.isBlank()) {
            throw new IllegalArgumentException("关联景点不能为空");
        }
        if (!landscapeRepository.existsById(landscapeId)) {
            throw new IllegalArgumentException("关联的景点不存在");
        }
        RecommendationPost post = new RecommendationPost();
        post.setRecomId(IdGenerator.next("REC"));
        post.setUserId(userId);
        post.setTitle(title.trim());
        post.setLandscapeId(landscapeId);
        post.setTag(tag != null ? tag.trim() : "");
        post.setContent(content.trim());
        post.setPublishTime(LocalDateTime.now());
        post.setAuditState("审核中");
        postRepository.save(post);
    }

    @Transactional
    public void updatePost(String recomId, String userId, String title, String landscapeId, String tag, String content) {
        RecommendationPost post = postRepository.findById(recomId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));
        if (!userId.equals(post.getUserId())) {
            throw new IllegalArgumentException("无权修改该帖子");
        }
        if (title != null && !title.isBlank()) {
            post.setTitle(title.trim());
        }
        post.setLandscapeId(landscapeId);
        if (tag != null) {
            post.setTag(tag.trim());
        }
        if (content != null && !content.isBlank()) {
            post.setContent(content.trim());
        }
        post.setAuditState("审核中");
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(String recomId, String userId) {
        logger.info("开始删除帖子, recomId={}, userId={}", recomId, userId);
        try {
            RecommendationPost post = postRepository.findById(recomId)
                    .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));
            logger.info("找到帖子: recomId={}, title={}", recomId, post.getTitle());

            if (!userId.equals(post.getUserId())) {
                throw new IllegalArgumentException("无权删除该帖子");
            }

            long commentCount = postCommentRepository.countByRecomId(recomId);
            long likeCount = postLikeRepository.countByRecomId(recomId);
            long collectCount = postCollectRepository.countByRecomId(recomId);
            logger.info("关联数据 - 评论:{}, 点赞:{}, 收藏:{}", commentCount, likeCount, collectCount);

            // 先删除通用表（通过子查询关联业务专属表，必须先删）
            commentRepository.deleteByRecomId(recomId);
            likeRepository.deleteByRecomId(recomId);
            collectRepository.deleteByRecomId(recomId);

            // 再删除推荐帖专属表的评论、点赞、收藏
            postCommentRepository.deleteByRecomId(recomId);
            logger.info("已删除评论");

            postLikeRepository.deleteByRecomId(recomId);
            logger.info("已删除点赞");

            postCollectRepository.deleteByRecomId(recomId);
            logger.info("已删除收藏");

            postRepository.deleteById(recomId);
            logger.info("帖子删除成功");
        } catch (Exception e) {
            logger.error("删除帖子失败, recomId={}, userId={}", recomId, userId, e);
            throw e;
        }
    }

    public String getLandscapeTitle(String landscapeId) {
        if (landscapeId == null || landscapeId.isBlank()) {
            return null;
        }
        return landscapeRepository.findById(landscapeId)
                .map(l -> "审核通过".equals(l.getAuditState()) ? l.getTitle() : null)
                .orElse(null);
    }

    public String getLandscapeTitleForAdmin(String landscapeId) {
        if (landscapeId == null || landscapeId.isBlank()) {
            return null;
        }
        return landscapeRepository.findById(landscapeId)
                .map(l -> l.getTitle())
                .orElse(null);
    }
}