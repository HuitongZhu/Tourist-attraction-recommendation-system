package com.travel.travelweb.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.travel.travelweb.entity.RecommendationPost;

public interface RecommendationPostRepository extends JpaRepository<RecommendationPost, String> {

    List<RecommendationPost> findByAuditStateOrderByPublishTimeDesc(String auditState, Pageable pageable);

    @Query("SELECT p FROM RecommendationPost p WHERE (:auditState IS NULL OR :auditState = '' OR p.auditState = :auditState) " +
           "AND (:keyword IS NULL OR :keyword = '' OR p.title LIKE CONCAT('%', :keyword, '%') OR p.tag LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.publishTime DESC")
    List<RecommendationPost> searchPosts(@Param("auditState") String auditState, @Param("keyword") String keyword);

    @Query("SELECT p FROM RecommendationPost p WHERE p.userId = :userId ORDER BY p.publishTime DESC")
    List<RecommendationPost> findByUserId(@Param("userId") String userId);

    // 查询某景点下的所有推荐帖
    List<RecommendationPost> findByLandscapeId(String landscapeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RecommendationPost p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RecommendationPost p WHERE p.landscapeId = :landscapeId")
    void deleteByLandscapeId(@Param("landscapeId") String landscapeId);
}
