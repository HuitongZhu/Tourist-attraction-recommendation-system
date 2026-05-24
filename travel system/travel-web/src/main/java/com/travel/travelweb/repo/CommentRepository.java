package com.travel.travelweb.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByUserIdOrderByPublishTimeDesc(String userId);
    long countByUserId(String userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.commentId IN (SELECT lc.commentId FROM LandComment lc WHERE lc.landscapeId = :landscapeId)")
    void deleteByLandscapeId(@Param("landscapeId") String landscapeId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.commentId IN (SELECT pc.commentId FROM PostComment pc WHERE pc.recomId = :recomId)")
    void deleteByRecomId(@Param("recomId") String recomId);
}