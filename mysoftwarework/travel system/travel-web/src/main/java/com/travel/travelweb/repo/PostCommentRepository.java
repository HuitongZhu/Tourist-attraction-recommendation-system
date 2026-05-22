package com.travel.travelweb.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.travelweb.entity.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, String> {

    List<PostComment> findByRecomIdOrderByPublishTimeDesc(String recomId);

    void deleteByRecomId(String recomId);

    void deleteByUserId(String userId);

    long countByRecomId(String recomId);
}