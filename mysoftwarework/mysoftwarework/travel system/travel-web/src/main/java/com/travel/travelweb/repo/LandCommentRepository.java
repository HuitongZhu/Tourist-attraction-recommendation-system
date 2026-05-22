package com.travel.travelweb.repo;

import com.travel.travelweb.entity.LandComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LandCommentRepository extends JpaRepository<LandComment, String> {

    List<LandComment> findByLandscapeIdOrderByPublishTimeDesc(String landscapeId);

    void deleteByUserId(String userId);

    void deleteByLandscapeId(String landscapeId);
}
