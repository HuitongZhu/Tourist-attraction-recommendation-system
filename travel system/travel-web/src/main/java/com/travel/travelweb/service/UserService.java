package com.travel.travelweb.service;

import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.CollectRepository;
import com.travel.travelweb.repo.CommentRepository;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.LikeRepository;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import com.travel.travelweb.repo.SysUserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    public static final String USER_TYPE_ADMIN = "1";
    public static final String USER_TYPE_ORDINARY = "2";

    private final SysUserRepository userRepository;
    private final LandCollectRepository landCollectRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final PostCollectRepository postCollectRepository;
    private final LandCommentRepository landCommentRepository;
    private final LandLikeRepository landLikeRepository;
    private final LandscapeRepository landscapeRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final RecommendationPostRepository recommendationPostRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final CollectRepository collectRepository;
    private final EntityManager entityManager;

    public UserService(SysUserRepository userRepository, 
                      LandCollectRepository landCollectRepository,
                      OrdinaryUserRepository ordinaryUserRepository,
                      PostCollectRepository postCollectRepository,
                      LandCommentRepository landCommentRepository,
                      LandLikeRepository landLikeRepository,
                      LandscapeRepository landscapeRepository,
                      PostCommentRepository postCommentRepository,
                      PostLikeRepository postLikeRepository,
                      RecommendationPostRepository recommendationPostRepository,
                      CommentRepository commentRepository,
                      LikeRepository likeRepository,
                      CollectRepository collectRepository,
                      EntityManager entityManager) {
        this.userRepository = userRepository;
        this.landCollectRepository = landCollectRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.postCollectRepository = postCollectRepository;
        this.landCommentRepository = landCommentRepository;
        this.landLikeRepository = landLikeRepository;
        this.landscapeRepository = landscapeRepository;
        this.postCommentRepository = postCommentRepository;
        this.postLikeRepository = postLikeRepository;
        this.recommendationPostRepository = recommendationPostRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.collectRepository = collectRepository;
        this.entityManager = entityManager;
    }

    public List<SysUser> findOrdinaryUsers(String keyword) {
        return userRepository.searchUsers(USER_TYPE_ORDINARY, keyword);
    }

    public Optional<SysUser> findById(String userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public boolean deleteUser(String userId) {
        if (userRepository.existsById(userId)) {
            // 删除用户的评论、点赞、收藏（通用表）
            commentRepository.deleteByUserId(userId);
            likeRepository.deleteByUserId(userId);
            collectRepository.deleteByUserId(userId);
            
            // 删除用户的景点收藏、评论、点赞
            landCollectRepository.deleteByUserId(userId);
            landCommentRepository.deleteByUserId(userId);
            entityManager.flush();
            entityManager.clear();
            landLikeRepository.deleteByUserId(userId);
            
            // 删除用户发布的景点（会自动删除景点关联的推荐帖及推荐帖的评论、点赞、收藏）
            List<com.travel.travelweb.entity.Landscape> landscapes = landscapeRepository.findByUserId(userId);
            for (com.travel.travelweb.entity.Landscape landscape : landscapes) {
                String landscapeId = landscape.getLandscapeId();
                
                // 删除景点关联的推荐帖及其评论、点赞、收藏
                List<com.travel.travelweb.entity.RecommendationPost> posts = recommendationPostRepository.findByLandscapeId(landscapeId);
                for (com.travel.travelweb.entity.RecommendationPost post : posts) {
                    // 先删除通用表
                    commentRepository.deleteByRecomId(post.getRecomId());
                    likeRepository.deleteByRecomId(post.getRecomId());
                    collectRepository.deleteByRecomId(post.getRecomId());
                    // 再删除业务专属表
                    postCommentRepository.deleteByRecomId(post.getRecomId());
                    postLikeRepository.deleteByRecomId(post.getRecomId());
                    postCollectRepository.deleteByRecomId(post.getRecomId());
                    recommendationPostRepository.deleteById(post.getRecomId());
                }
                
                // 先删除通用表（通过子查询关联业务专属表，必须先删）
                collectRepository.deleteByLandscapeId(landscapeId);
                likeRepository.deleteByLandscapeId(landscapeId);
                commentRepository.deleteByLandscapeId(landscapeId);
                
                // 再删除景点专属表
                landCollectRepository.deleteByLandscapeId(landscapeId);
                landLikeRepository.deleteByLandscapeId(landscapeId);
                landCommentRepository.deleteByLandscapeId(landscapeId);
                
                // 删除景点
                landscapeRepository.deleteById(landscapeId);
            }
            
            // 删除用户的推荐帖评论、点赞、收藏
            postCommentRepository.deleteByUserId(userId);
            postLikeRepository.deleteByUserId(userId);
            postCollectRepository.deleteByUserId(userId);
            
            // 删除通用表中用户的评论、点赞、收藏
            commentRepository.deleteByUserId(userId);
            likeRepository.deleteByUserId(userId);
            collectRepository.deleteByUserId(userId);
            
            // 删除用户发布的推荐帖（未关联景点的）
            List<com.travel.travelweb.entity.RecommendationPost> posts = recommendationPostRepository.findByUserId(userId);
            for (com.travel.travelweb.entity.RecommendationPost post : posts) {
                // 先删除通用表（通过子查询关联业务专属表，必须先删）
                commentRepository.deleteByRecomId(post.getRecomId());
                likeRepository.deleteByRecomId(post.getRecomId());
                collectRepository.deleteByRecomId(post.getRecomId());
                
                // 再删除推荐帖专属表的评论、点赞、收藏
                postCommentRepository.deleteByRecomId(post.getRecomId());
                postLikeRepository.deleteByRecomId(post.getRecomId());
                postCollectRepository.deleteByRecomId(post.getRecomId());
                
                recommendationPostRepository.deleteById(post.getRecomId());
            }
            
            // 删除用户详情
            ordinaryUserRepository.deleteByUserId(userId);
            entityManager.flush();
            entityManager.clear();
            
            // 删除用户主表
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateUserName(String userId, String newUserName) {
        return userRepository.findById(userId).map(user -> {
            user.setUserName(newUserName);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }
}
