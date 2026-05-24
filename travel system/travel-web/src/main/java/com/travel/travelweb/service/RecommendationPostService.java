package com.travel.travelweb.service;

import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.repo.CollectRepository;
import com.travel.travelweb.repo.CommentRepository;
import com.travel.travelweb.repo.LikeRepository;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommendationPostService {

    public static final String AUDIT_PENDING = "待审核";
    public static final String AUDIT_APPROVED = "审核通过";
    public static final String AUDIT_REJECTED = "审核未通过";

    private final RecommendationPostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final CollectRepository collectRepository;

    public RecommendationPostService(RecommendationPostRepository postRepository,
                                    PostCommentRepository postCommentRepository,
                                    PostLikeRepository postLikeRepository,
                                    PostCollectRepository postCollectRepository,
                                    CommentRepository commentRepository,
                                    LikeRepository likeRepository,
                                    CollectRepository collectRepository) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.collectRepository = collectRepository;
    }

    public List<RecommendationPost> findPosts(String auditState, String keyword) {
        return postRepository.searchPosts(auditState, keyword);
    }

    @Transactional
    public boolean updateAuditState(String recomId, String auditState) {
        return postRepository.findById(recomId).map(post -> {
            post.setAuditState(auditState);
            postRepository.save(post);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean deletePost(String recomId) {
        // 先检查帖子是否存在
        if (!postRepository.existsById(recomId)) {
            return false;
        }
        // 先删除通用表（通过子查询关联业务专属表，必须先删）
        commentRepository.deleteByRecomId(recomId);
        likeRepository.deleteByRecomId(recomId);
        collectRepository.deleteByRecomId(recomId);
        
        // 再删除推荐帖专属表的评论、点赞和收藏
        postCommentRepository.deleteByRecomId(recomId);
        postLikeRepository.deleteByRecomId(recomId);
        postCollectRepository.deleteByRecomId(recomId);
        
        // 使用 deleteById 而不是 delete(entity)，避免缓存问题
        postRepository.deleteById(recomId);
        return true;
    }
}
