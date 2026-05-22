package com.travel.travelweb.service;

import com.travel.travelweb.entity.RecommendationPost;
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

    public RecommendationPostService(RecommendationPostRepository postRepository,
                                    PostCommentRepository postCommentRepository,
                                    PostLikeRepository postLikeRepository,
                                    PostCollectRepository postCollectRepository) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
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
        return postRepository.findById(recomId).map(post -> {
            // 先删除关联的评论、点赞和收藏
            postCommentRepository.deleteByRecomId(recomId);
            postLikeRepository.deleteByRecomId(recomId);
            postCollectRepository.deleteByRecomId(recomId);
            // 再删除帖子
            postRepository.delete(post);
            return true;
        }).orElse(false);
    }
}
