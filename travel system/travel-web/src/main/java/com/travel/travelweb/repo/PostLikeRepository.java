package com.travel.travelweb.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.PostLike;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, String> {
    boolean existsByRecomIdAndUserId(String recomId, String userId);
    Optional<PostLike> findByRecomIdAndUserId(String recomId, String userId);
    long countByRecomId(String recomId);
    List<PostLike> findByUserIdOrderByLikeTimeDesc(String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostLike pl WHERE pl.recomId = :recomId")
    void deleteByRecomId(@Param("recomId") String recomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostLike pl WHERE pl.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}