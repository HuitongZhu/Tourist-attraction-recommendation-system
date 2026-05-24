package com.travel.travelweb.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.Collect;

@Repository
public interface CollectRepository extends JpaRepository<Collect, String> {
    List<Collect> findByUserIdOrderByCollectTimeDesc(String userId);
    long countByUserId(String userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Collect c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Collect c WHERE c.collectId IN (SELECT lc.collectId FROM LandCollect lc WHERE lc.landscapeId = :landscapeId)")
    void deleteByLandscapeId(@Param("landscapeId") String landscapeId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Collect c WHERE c.collectId IN (SELECT pc.collectId FROM PostCollect pc WHERE pc.recomId = :recomId)")
    void deleteByRecomId(@Param("recomId") String recomId);
}