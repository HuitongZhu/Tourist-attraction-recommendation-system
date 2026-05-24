package com.travel.travelweb.repo;

import com.travel.travelweb.entity.LandComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LandCommentRepository extends JpaRepository<LandComment, String> {

    List<LandComment> findByLandscapeIdOrderByPublishTimeDesc(String landscapeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LandComment lc WHERE lc.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LandComment lc WHERE lc.landscapeId = :landscapeId")
    void deleteByLandscapeId(@Param("landscapeId") String landscapeId);
}
