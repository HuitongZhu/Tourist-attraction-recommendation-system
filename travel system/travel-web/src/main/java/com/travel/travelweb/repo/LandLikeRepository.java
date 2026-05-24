package com.travel.travelweb.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.travel.travelweb.entity.LandLike;

public interface LandLikeRepository extends JpaRepository<LandLike, String> {

    boolean existsByLandscapeIdAndUserId(String landscapeId, String userId);

    Optional<LandLike> findByLandscapeIdAndUserId(String landscapeId, String userId);

    long countByLandscapeId(String landscapeId);

    List<LandLike> findByUserIdOrderByLikeTimeDesc(String userId);

    @Query("SELECT ll.landscapeId, COUNT(ll) FROM LandLike ll WHERE ll.landscapeId IN :ids GROUP BY ll.landscapeId")
    List<Object[]> countByLandscapeIdIn(@Param("ids") List<String> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LandLike ll WHERE ll.landscapeId = :landscapeId")
    void deleteByLandscapeId(@Param("landscapeId") String landscapeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LandLike ll WHERE ll.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}