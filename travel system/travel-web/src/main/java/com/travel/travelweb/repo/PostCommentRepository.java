package com.travel.travelweb.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.PostComment;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, String> {

    List<PostComment> findByRecomIdOrderByPublishTimeDesc(String recomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostComment pc WHERE pc.recomId = :recomId")
    void deleteByRecomId(@Param("recomId") String recomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostComment pc WHERE pc.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    long countByRecomId(String recomId);
}