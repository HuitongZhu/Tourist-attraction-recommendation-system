package com.travel.travelweb.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.PostCollect;

@Repository
public interface PostCollectRepository extends JpaRepository<PostCollect, String> {
    boolean existsByRecomIdAndUserId(String recomId, String userId);
    Optional<PostCollect> findByRecomIdAndUserId(String recomId, String userId);
    List<PostCollect> findByUserIdOrderByCollectTimeDesc(String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostCollect pc WHERE pc.recomId = :recomId")
    void deleteByRecomId(@Param("recomId") String recomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostCollect pc WHERE pc.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostCollect pc WHERE pc.recomId IN " +
           "(SELECT p.recomId FROM RecommendationPost p WHERE p.landscapeId = :landscapeId)")
    void deleteByLandscapeId(@Param("landscapeId") String landscapeId);

    long countByRecomId(String recomId);
}