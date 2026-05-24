package com.travel.travelweb.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {
    List<Like> findByUserIdOrderByLikeTimeDesc(String userId);
    long countByUserId(String userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Like l WHERE l.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Like l WHERE l.likeId IN (SELECT ll.likeId FROM LandLike ll WHERE ll.landscapeId = :landscapeId)")
    void deleteByLandscapeId(@Param("landscapeId") String landscapeId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Like l WHERE l.likeId IN (SELECT pl.likeId FROM PostLike pl WHERE pl.recomId = :recomId)")
    void deleteByRecomId(@Param("recomId") String recomId);
}